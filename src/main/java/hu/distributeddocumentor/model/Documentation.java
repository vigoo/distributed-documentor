package hu.distributeddocumentor.model;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.inject.Inject;
import hu.distributeddocumentor.gui.LongOperationRunner;
import hu.distributeddocumentor.gui.ProgressUI;
import hu.distributeddocumentor.gui.RunnableWithProgress;
import hu.distributeddocumentor.model.toc.DefaultTOCNodeFactory;
import hu.distributeddocumentor.model.toc.TOC;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.utils.CaseInsensitiveMap;
import hu.distributeddocumentor.utils.RepositoryUriGenerator;
import hu.distributeddocumentor.utils.ResourceUtils;
import hu.distributeddocumentor.vcs.VersionControl;
import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Documentation is the root of the data model storing a documentation
 *
 * <p>
 * The documentation consists of pages, snippets (which can be included into
 * pages), images and a TOC.
 * <p>
 * The documentation itself is stored in a version controlled repository, which
 * is currently fixed to Mercurial.
 *
 * @author Daniel Vigovszky
 */
public class Documentation extends Observable implements Observer, SnippetCollection {
    // TODO: separate into smaller classes

    private static final Logger log = LoggerFactory.getLogger(Documentation.class.getName());

    private final VersionControl versionControl;    
    private final TOC toc;
    private final Map<String, Page> pages;
    private final Map<String, Snippet> snippets;
    private Images images;
    private String title = "Documentation";
    private boolean titleHasChanged;

    private String relativeRoot;
    private final DocumentorPreferences prefs;
    private int orphanedPageProcessingSuspended;

    /**
     * Gets the table of contents for this documentation
     *
     * @return the table of contents - never null
     */
    public TOC getTOC() {
        return toc;
    }

    /**
     * Gets access to the version control storing this documentation
     *
     * @return the version control interface
     */
    public VersionControl getVersionControl() {
        return versionControl;
    }

    /**
     * Gets the snippets contained by the documentation
     *
     * <p>
     * Use addSnippet to add a new snippet to the documentation!
     *
     * @return the collection of snippets
     * @see Snippet
     */
    @Override
    public Collection<Snippet> getSnippets() {
        return snippets.values();
    }

    /**
     * Gets the documentation title
     *
     * @return title of the documentation
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the documentation title
     *
     * @param title title of the documentation
     */
    public void setTitle(String title) {

        if (!title.equals(this.title)) {
            this.title = title;
            titleHasChanged = true;
        }
    }

    /**
     * Constructs an uninitialized documentation object
     *
     * <p>
     * After creating the object, one of the following methods must be called
     * before doing anything else: initAsNew initFromExisting cloneFromRemote
     *
     * @param versionControl the version control interface
     * @param prefs the application preferences to be used
     */
    @Inject
    public Documentation(VersionControl versionControl, DocumentorPreferences prefs) {

        this.versionControl = versionControl; 
        
        toc = new TOC(this, new DefaultTOCNodeFactory());
        pages = new CaseInsensitiveMap<>();
        snippets = new CaseInsensitiveMap<>();

        this.prefs = prefs;
    }

    /**
     * Initializes a new, empty documentation project at the given root
     * directory.
     *
     * <p>
     * A new repository will be created and some default files immediately added
     * (start page, TOC).
     *
     * @param repositoryRoot the new, empty folder for the documentation
     * @throws IOException
     */
    public void initAsNew(File repositoryRoot) throws IOException {

        versionControl.create(repositoryRoot);
        relativeRoot = "";

        images = new Images(versionControl, relativeRoot);

        Page first = new Page("start", this, prefs.getConditions());

        try {
            addNewPage(first);
        } catch (PageAlreadyExistsException ex) {
            throw new IllegalStateException("Must be called on a fresh instance!");
        }

        try {
            toc.save(repositoryRoot);            
            versionControl.add(new File(repositoryRoot, "toc.xml"));            
        } catch (FileNotFoundException | TransformerException ex) {
            log.error(null, ex);

            throw new IllegalStateException("Must be called on a fresh instance!");
        }

        File snippetsDir = getSnippetsDirectory();
        if (!snippetsDir.exists()) {
            if (!snippetsDir.mkdirs()) {
                throw new RuntimeException("Failed to create snippets directory!");
            }
        }
    }

    /**
     * Loads an existing documentation from a local directory
     *
     * @param repositoryRoot the root directory of the documentation
     * @param longOp Interface to run long operations
     * @throws FailedToLoadPageException
     * @throws FailedToLoadTOCException
     */
    public void initFromExisting(File repositoryRoot, LongOperationRunner longOp) throws FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {

        final File realRepositoryRoot = findRealRepositoryRoot(repositoryRoot);
        relativeRoot = ResourceUtils.getRelativePath(repositoryRoot.getAbsolutePath(), realRepositoryRoot.getAbsolutePath());

        log.info("Specified root: " + repositoryRoot.toString());
        log.info("Real root:      " + realRepositoryRoot.toString());
        log.info("Relative root:  " + relativeRoot);

        longOp.run(new RunnableWithProgress() {

            @Override
            public void run(ProgressUI progress) {
                
                versionControl.open(realRepositoryRoot);
                images = new Images(versionControl, relativeRoot);

                progress.setStatus("Fixing missing files...");
                fixMissingFiles();
                try {
                    progress.setStatus("Loading documentation...");
                    loadRepository();
                } catch (FailedToLoadPageException | FailedToLoadTOCException | FailedToLoadMetadataException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Clones a remote repository to a local folder and loads its contents
     *
     * @param localRepositoryRoot the local folder to contain the documentation
     * @param remoteRepo the remote repository's URL or path
     * @param userName user name to be used for http authentication
     * @param password password to be used for http authentication
     * @param longOp Interface to run long operations
     * @throws FailedToLoadPageException
     * @throws FailedToLoadTOCException
     */
    public void cloneFromRemote(final File localRepositoryRoot, final String remoteRepo, final String userName, final String password, LongOperationRunner longOp) throws FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {

        relativeRoot = "";
        longOp.run(new RunnableWithProgress() {

            @Override
            public void run(ProgressUI progress) {
                progress.setStatus("Cloning remote repository...");
                versionControl.clone(localRepositoryRoot, RepositoryUriGenerator.addCredentials(remoteRepo, userName, password));                
                images = new Images(versionControl, relativeRoot);

                progress.setStatus("Fixing missing files...");
                fixMissingFiles();
                try {
                    progress.setStatus("Loading documentation...");
                    loadRepository();
                } catch (FailedToLoadPageException | FailedToLoadTOCException | FailedToLoadMetadataException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private File getDocumentationDirectory() {
        return new File(versionControl.getRoot(), relativeRoot);
    }

    private File getSnippetsDirectory() {
        return new File(getDocumentationDirectory(), "snippets");
    }

    private void loadRepository() throws FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {

        File snippetsDir = getSnippetsDirectory();
        if (!snippetsDir.exists()) {
            if (!snippetsDir.mkdirs()) {
                throw new RuntimeException("Failed to create snippets directory!");
            }
        } else {
            for (File child : snippetsDir.listFiles(
                    new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isFile()
                            && !"toc.xml".equals(file.getName())
                            && isSupportedMarkup(file);
                        }
                    })) {
                try {
                    Snippet snippet = new Snippet(child, this, prefs.getConditions());
                    registerSnippet(snippet);
                } catch (IOException ex) {
                    throw new FailedToLoadPageException(child, ex);
                }
            }
        }

        for (File child : getDocumentationDirectory().listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile()
                        && !"toc.xml".equals(file.getName())
                        && isSupportedMarkup(file);
                    }
                })) {

            try {
                Page page = new Page(child, this, prefs.getConditions());
                registerPage(page);
            } catch (IOException ex) {
                throw new FailedToLoadPageException(child, ex);
            }
        }

        try {
            toc.load(getDocumentationDirectory(), this);
        } catch (SAXException | IOException | ParserConfigurationException | ClassNotFoundException ex) {
            throw new FailedToLoadTOCException(ex);
        }

        // Adding unreferenced pages to the unorganized node
        for (Page page : pages.values()) {

            if (!toc.isReferenced(page)) {
                toc.addUnorganized(toc.getFactory().createNode(page));
            }
        }

        // Loading documentation propeties
        try {
            loadProperties();
        } catch (IOException ex) {
            throw new FailedToLoadMetadataException(ex);
        }
    }

    /**
     * Reloads the whole documentation
     *
     * <p>
     * This is useful if the repository has been updated.
     *
     * @throws FailedToLoadPageException
     * @throws FailedToLoadTOCException
     * @throws hu.distributeddocumentor.model.FailedToLoadMetadataException
     */
    public void reload() throws FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {

        toc.clear();
        pages.clear();
        images.reload();

        loadRepository();
    }

    private boolean isSupportedMarkup(File file) {
        return file.getName().toLowerCase().endsWith(".mediawiki");
    }

    /**
     * Adds a new page to the documentation
     *
     * @param page the page to be added
     * @throws PageAlreadyExistsException If a page is already added with the
     * same identifier
     * @throws IOException
     * @see Page
     */
    public void addNewPage(Page page) throws PageAlreadyExistsException, IOException {

        String id = page.getId();
        if (!pages.containsKey(id)) {
            registerPage(page);
        } else {
            throw new PageAlreadyExistsException();
        }

        if (!toc.getReferencedPages().contains(id)) {
            toc.addToEnd(toc.getUnorganized(), toc.getFactory().createNode(page));
        }

        File[] pageFiles = page.save(getDocumentationDirectory());

        for (File pageFile : pageFiles) {
            log.info("Adding new file to repository: " + pageFile.getName());
        }

        versionControl.add(pageFiles);        
    }

    private void registerPage(Page page) {
        pages.put(page.getId(), page);

        page.addObserver(this);
    }

    private void registerSnippet(Snippet snippet) {
        snippets.put(snippet.getId(), snippet);

        snippet.addObserver(this);
    }

    /**
     * Gets all the pages in the documentation
     *
     * @return the collection of pages
     */
    public Collection<Page> getPages() {
        return pages.values();
    }

    /**
     * Gets a page by its identifier
     *
     * @param id the page identifier
     * @return returns the page or null if it is not available
     * @see Page
     */
    public Page getPage(String id) {
        return pages.get(id);
    }

    /**
     * Gets the image collection for this documentation
     *
     * @return the image collection, never null
     */
    public Images getImages() {
        return images;
    }

    /**
     * Saves every modified page and snippet, and the TOC
     *
     * <p>
     * This method only modifies the tracked files, but does not invoke commit
     * on the repository!
     *
     * @throws CouldNotSaveDocumentationException
     */
    public void saveAll() throws CouldNotSaveDocumentationException {

        File root = getDocumentationDirectory();

        try {
            for (Page page : pages.values()) {
                if (page.saveIfModified(root)) {
                    ensurePageFilesAdded(page, getDocumentationDirectory());
                }
            }

            File snippetsDir = getSnippetsDirectory();
            for (Snippet snippet : snippets.values()) {
                if (snippet.saveIfModified(snippetsDir)) {
                    ensurePageFilesAdded(snippet, getSnippetsDirectory());
                }
            }

            toc.saveIfModified(root);

            if (titleHasChanged) {
                saveProperties();
            }
        } catch (IOException | TransformerException ex) {
            log.error(null, ex);

            throw new CouldNotSaveDocumentationException(ex);
        }
    }

    private File getDocumentationMetadataFile() {
        return new File(getDocumentationDirectory(), "documentation.properties");
    }

    private void saveProperties() throws IOException {

        File metadataFile = getDocumentationMetadataFile();

        boolean alreadyExisted = metadataFile.exists();

        Properties metadata = new Properties();
        metadata.put("title", title);

        try (OutputStream out = new FileOutputStream(metadataFile)) {
            metadata.store(out, "Global documentation properties");
        }

        if (!alreadyExisted) {
            versionControl.add(metadataFile);            
        }

        // Removing comments to avoid merging conflicts                     
        // TODO: merge with PageMetadata's similar code
        final List<String> lines = Files.readLines(metadataFile, Charset.defaultCharset());
        lines.remove(0);
        lines.remove(0);
        Files.write(Joiner.on('\n').join(lines), metadataFile, Charset.defaultCharset());

        titleHasChanged = false;
    }

    private void loadProperties() throws IOException {

        File metadataFile = getDocumentationMetadataFile();
        Properties metadata = new Properties();
        if (metadataFile.exists()) {

            try (InputStream in = new FileInputStream(metadataFile)) {
                metadata.load(in);
            } catch (IOException ex) {
                log.error("Failed to load documentation metadata", ex);
                metadata.clear();
            }
        }

        if (metadata.containsKey("title")) {
            title = metadata.getProperty("title");
        }
    }


    /**
     * Revert a set of changes
     *
     * <p>
     * Use the getChanges method to get the list of modified files before
     * calling this method!
     *
     * @param files the relative path of files to be reverted
     * @throws FailedToLoadPageException
     * @throws FailedToLoadTOCException
     * @throws hu.distributeddocumentor.model.FailedToLoadMetadataException
     */
    public void revertChanges(List<String> files) throws FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {

        String[] items = Arrays.copyOf(files.toArray(), files.size(), String[].class);                
        
        versionControl.revert(items);

        reload();
    }

    /**
     * Gets the repository's root directory.
     *
     * <p>
     * This is not always the same as the documentation's root directory!
     * Documentations stored in a subdirectory of a repository are also
     * supported.
     *
     * @return returns the absolute path of the repository
     */
    public String getRepositoryRoot() {
        return getDocumentationDirectory().getAbsolutePath();
    }

    @Override
    public void update(Observable o, Object o1) {

        if (o instanceof Snippet) {

            Snippet snippet = (Snippet) o;

            for (Page page : pages.values()) {

                if (page.referencesSnippet(snippet)) {
                    page.refresh();
                }
            }
        } else if (o instanceof Page) {

            Page page = (Page) o;

            for (String pageId : page.getReferencedPages()) {

                if (!pages.containsKey(pageId)) {

                    Page newPage = new Page(pageId, this, prefs.getConditions());

                    try {
                        addNewPage(newPage);
                    } catch (IOException | PageAlreadyExistsException ex) {
                        log.error(null, ex);
                    }
                } else {

                    Page existingPage = pages.get(pageId);
                    if (toc.isInRecycleBin(existingPage)) {

                        // If a reference has been created to a page which is in the 
                        // recycle bin, we move it to the unorganized pages node
                        toc.removeFromRecycleBin(existingPage);
                        toc.addToEnd(toc.getUnorganized(), toc.getFactory().createNode(existingPage));

                    }
                }
            }
        }
    }

    public void suspendProcessingOrphanedPages() {
        orphanedPageProcessingSuspended++;
    }

    public void resumeProcessingOrphanedPages() {
        orphanedPageProcessingSuspended--;
    }

    /**
     * Looks for orphaned pages and moves them to the recycle bin node in the
     * TOC. Pages which has not been changed from the original template are
     * immediately deleted and removed from the repository.
     */
    public void processOrphanedPages() {

        if (orphanedPageProcessingSuspended == 0) {
            log.info("Processing orphaned pages...");

            // Collect all references
            Set<String> referencedPages = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

            // 'start' is always referenced
            referencedPages.add("start");

            // collecting pages which are referenced from other pages
            for (Page page : pages.values()) {
                referencedPages.addAll(page.getReferencedPages());
            }

        // collecting pages which are referenced in the TOC (except in the
            // unorganized pages or recycle bin nodes)
            referencedPages.addAll(toc.getReferencedPages());

            // Collect the pages which are NOT referenced
            Set<String> orphanedPages = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            orphanedPages.addAll(pages.keySet());
            orphanedPages.removeAll(referencedPages);

            for (String pageId : orphanedPages) {

                log.info("Found orphaned page: " + pageId);

                Page page = pages.get(pageId);

                // If the page does not equals the default template
                if (!page.equalsTemplate()) {
                // ..then we don't delete it, but put into the recycle bin
                    // node instead of the unorganized pages node

                    // Checking if it is already in the recycle bin
                    if (!toc.isInRecycleBin(page)) {

                        // ..if not, we remove it from wherever it is and put it there
                        log.info(" -> putting it to recycle bin");

                        toc.remove(page);
                        toc.addToEnd(toc.getRecycleBin(), toc.getFactory().createNode(page));
                    }
                } else {

                    log.info(" -> was not modified, removing it");

                // ..otherwise we don't keep reference to it in the TOC and
                    // delete it from the repository as well                  
                    deletePage(page);
                }
            }

            log.info("Finished processing orphaned pages.");
        }
    }

    private File findRealRepositoryRoot(File repositoryRoot) {
        boolean found = false;

        if (repositoryRoot.isDirectory()) {

            File hgdir = new File(repositoryRoot, ".hg");
            if (hgdir.exists()
                    && hgdir.isDirectory()) {
                found = true;
            }
        }

        if (found) {
            return repositoryRoot;
        } else {
            File parent = repositoryRoot.getParentFile();
            if (parent != null) {
                return findRealRepositoryRoot(parent);
            } else {
                return null;
            }
        }
    }

    /**
     * Gets a snippet by its identifier
     *
     * @param id the identifier of the snippet
     * @return returns the snippet, or null if it does not exist
     * @see Snippet
     */
    @Override
    public Snippet getSnippet(String id) {
        return snippets.get(id);
    }

    /**
     * Adds a new snippet to the documentation
     *
     * @param snippet the new snippet to be added
     * @throws IOException
     * @throws PageAlreadyExistsException When a snippet with the same
     * identifier is already added to the documentation
     */
    @Override
    public void addSnippet(Snippet snippet) throws IOException, PageAlreadyExistsException {

        String id = snippet.getId();
        if (!snippets.containsKey(id)) {
            registerSnippet(snippet);
        } else {
            throw new PageAlreadyExistsException();
        }

        File[] snippetFiles = snippet.save(getSnippetsDirectory());

        for (File snippetFile : snippetFiles) {
            log.info("Adding new snippet to repository: " + snippetFile.getName());
        }

        versionControl.add(snippetFiles);        

        setChanged();
        notifyObservers();
    }

    /**
     * Removes a snippet from the documentation (and the repository)
     *
     * @param id the identifier of the snippet
     */
    @Override
    public void removeSnippet(String id) {

        log.info("Removing snippet " + id + " from repository");

        Snippet snippet = snippets.get(id);
        snippets.remove(id);
        
        File[] files = snippet.getFiles(getSnippetsDirectory());
        versionControl.remove(files, true, false);

        for (File f : files) {
            if (!f.delete()) {
                log.error("Failed to delete snippet file " + f.getName());
            }
        }

        setChanged();
        notifyObservers();
    }

    private void fixMissingFiles() {

        List<String> toRemove = new LinkedList<>();
        for (String missing : versionControl.getMissingFiles()) {

            File root = new File(getRepositoryRoot());
            File missingFile = new File(root, missing);

            if (missingFile.getAbsolutePath().startsWith(getDocumentationDirectory().getAbsolutePath())) {
                log.info("Forgetting missing file " + missing);
                toRemove.add(missing);
            } else {
                log.info("Leaving missing file " + missing);
            }
        }

        if (!toRemove.isEmpty()) {
            versionControl.remove(toRemove.toArray(new String[0]), true, true);            
        }
    }

    private void ensurePageFilesAdded(Page page, File root) {

        File[] files = page.getFiles(root);
        for (File f : files) {
            if (f.exists()) {

                log.debug("Checking status of " + f.getName());

                if (!versionControl.isAdded(f)) {

                    log.debug(" -> status is unknown, adding to repository...");

                    versionControl.add(f);                    
                }
            }
        }
    }

    /**
     * Gets the color associated with given status values
     *
     * @param status the status string queried
     * @return returns a color which can be used as background color
     * representing the queried status. The default is white.
     */
    public Color getStatusColor(String status) {
        // TODO: make it user configurable
        if (status != null) {
            switch (status) {
                case "Reviewed":
                    return new Color(192, 255, 192, 255);
                case "Completed":
                    return Color.GREEN;
                case "In progress":
                    return Color.YELLOW;
                case "Not started":
                    return Color.WHITE;
                default:
                    return Color.WHITE;
            }
        } else {
            return Color.WHITE;
        }
    }

    /**
     * Changes the given page's identifier and modifies every other page that
     * refers to it.
     *
     * @param page Page to be changed
     * @param newId New identifier of the page
     * @throws hu.distributeddocumentor.model.CouldNotSaveDocumentationException
     * @throws hu.distributeddocumentor.model.FailedToLoadPageException
     * @throws hu.distributeddocumentor.model.FailedToLoadTOCException
     * @throws hu.distributeddocumentor.model.FailedToLoadMetadataException
     */
    public void renamePage(Page page, String newId) throws CouldNotSaveDocumentationException, FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {

        try {
            if (!page.getId().equals(newId)) {

                // Modify related pages
                for (Page otherPage : pages.values()) {
                    if (otherPage != page) {
                        otherPage.modifyPageReferences(page.getId(), newId);
                    }
                }

                saveAll();

                // Rename the page            
                for (File pageFile : page.getFiles(getDocumentationDirectory())) {
                                        
                    versionControl.rename(pageFile, new File(pageFile.toString().replace(page.getId(), newId)), true);                   
                }

                page.reload(getDocumentationDirectory(), newId);
                toc.save(getDocumentationDirectory());

                reload();
            }
        } catch (IOException | TransformerException ex) {
            log.error(null, ex);

            throw new CouldNotSaveDocumentationException(ex);
        }
    }

    /**
     * Deletes a page permanently from the documentation
     *
     * This method does not use the TOC's recycle bin to keep the deleted page,
     * it will be deleted from the repository immediately.
     *
     * @param page Page to be deleted
     */
    public void deletePage(Page page) {
        toc.remove(page);
        pages.remove(page.getId());
        
        File[] files = page.getFiles(getDocumentationDirectory());
        
        versionControl.remove(files, true, false);

        for (File f : files) {
            boolean deleteSucceeded = f.delete();
            if (!deleteSucceeded) {
                log.error("Failed to delete file " + f.getName());
            }
        }
    }

}
