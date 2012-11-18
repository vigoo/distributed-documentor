package hu.distributeddocumentor.model;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.RepositoryConfiguration;
import com.aragost.javahg.commands.*;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.utils.CaseInsensitiveMap;
import hu.distributeddocumentor.utils.RepositoryUriGenerator;
import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.CodingErrorAction;
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
    // TODO: hide version control implementation behind an abstraction
    
    private static final Logger log = LoggerFactory.getLogger(Documentation.class.getName());
    
    private final TOC toc;
    private final Map<String, Page> pages;    
    private final Map<String, Snippet> snippets;
    private Images images;
    
    private String relativeRoot;
    private Repository repository;
    private final DocumentorPreferences prefs;

    /**
     * Gets the table of contents for this documentation
     * 
     * @return the table of contents - never null
     */
    public TOC getTOC() {
        return toc;
    }

    /**
     * Gets the Mercurial repository storing this documentation
     * 
     * @return the Mercurial repository 
     */
    public Repository getRepository() {
        return repository;
    }        

    /**
     * Gets the snippets contained by the documentation
     * 
     * <p>
     * Use addSnippet to add a new snippet to the documentation!
     
     * @return the collection of snippets
     * @see Snippet     */
    @Override
    public Collection<Snippet> getSnippets() {
        return snippets.values();
    }
    
    /**
     * Constructs an uninitialized documentation object
     * 
     * <p>
     * After creating the object, one of the following methods
     * must be called before doing anything else:
     * initAsNew
     * initFromExisting
     * cloneFromRemote
     * 
     * @param prefs the application preferences to be used
     */
    public Documentation(DocumentorPreferences prefs) {
        
        toc = new TOC();
        pages = new CaseInsensitiveMap<>();
        snippets = new CaseInsensitiveMap<>();
        
        this.prefs = prefs;
    }
    
    /**
     * Initializes a new, empty documentation project at the given root directory.
     * 
     * <p>
     * A new repository will be created and some default files immediately 
     * added (start page, TOC).
     * 
     * @param repositoryRoot the new, empty folder for the documentation
     * @throws IOException
     */
    public void initAsNew(File repositoryRoot) throws IOException {
                
        repository = Repository.create(createRepositoryConfiguration(), repositoryRoot);
        relativeRoot = "";
        
        images = new Images(repository, relativeRoot);
        
        Page first = new Page("start", this);
        
        try {
            addNewPage(first);
        }
        catch (PageAlreadyExistsException ex) {
            throw new IllegalStateException("Must be called on a fresh instance!");
        }

        try {
            toc.save(repositoryRoot);
            AddCommand cmd = new AddCommand(repository);
            cmd.execute(new File(repositoryRoot, "toc.xml"));        
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
     * @throws FailedToLoadPageException
     * @throws FailedToLoadTOCException
     */
    public void initFromExisting(File repositoryRoot) throws FailedToLoadPageException, FailedToLoadTOCException {
        
        File realRepositoryRoot = findRealRepositoryRoot(repositoryRoot);
        relativeRoot = realRepositoryRoot.toURI().relativize(repositoryRoot.toURI()).getPath();
        
        log.info("Specified root: " + repositoryRoot.toString());
        log.info("Real root:      " + realRepositoryRoot.toString());
        log.info("Relative root:  " + relativeRoot);
        
        repository = Repository.open(createRepositoryConfiguration(), realRepositoryRoot);
        images = new Images(repository, relativeRoot);
        
        fixMissingFiles();
        loadRepository();        
    }
    
    /**
     * Clones a remote repository to a local folder and loads its contents
     * 
     * @param localRepositoryRoot the local folder to contain the documentation
     * @param remoteRepo the remote repository's URL or path
     * @param userName user name to be used for http authentication
     * @param password password to be used for http authentication
     * @throws FailedToLoadPageException
     * @throws FailedToLoadTOCException
     */
    public void cloneFromRemote(File localRepositoryRoot, String remoteRepo, String userName, String password) throws FailedToLoadPageException, FailedToLoadTOCException {
                
        relativeRoot = "";
        repository = Repository.clone(createRepositoryConfiguration(), localRepositoryRoot, RepositoryUriGenerator.addCredentials(remoteRepo, userName, password));
        images = new Images(repository, relativeRoot);
        
        fixMissingFiles();
        loadRepository();
    }
    
    private RepositoryConfiguration createRepositoryConfiguration() {
        
        RepositoryConfiguration conf = new RepositoryConfiguration();
        conf.setHgBin(prefs.getMercurialPath());
        conf.setCodingErrorAction(CodingErrorAction.REPLACE);
        
        return conf;
    }
    
    private File getDocumentationDirectory() {
        return new File(repository.getDirectory(), relativeRoot);
    }
    
    private File getSnippetsDirectory() {
        return new File(getDocumentationDirectory(), "snippets");
    }
    
    private void loadRepository() throws FailedToLoadPageException, FailedToLoadTOCException {

        File snippetsDir = getSnippetsDirectory();
        if (!snippetsDir.exists()) {
            if (!snippetsDir.mkdirs()) {
                throw new RuntimeException("Failed to create snippets directory!");
            }
        }
        else {
            for (File child : snippetsDir.listFiles(
                    new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() &&
                               !"toc.xml".equals(file.getName()) &&                               
                               isSupportedMarkup(file);
                    }})) {
                try {
                    Snippet snippet = new Snippet(child, this);                                        
                    registerSnippet(snippet);
                }
                catch (Exception ex) {
                    throw new FailedToLoadPageException(child, ex);
                }
            }
        }
        
        for (File child : getDocumentationDirectory().listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() &&
                               !"toc.xml".equals(file.getName()) &&                               
                               isSupportedMarkup(file);
                    }
        })) {
            
            try {
                Page page = new Page(child, this);
                registerPage(page);
            }
            catch (Exception ex) {                
                throw new FailedToLoadPageException(child, ex);
            }
        }                
        
        try {
            toc.load(getDocumentationDirectory(), this);
        }
        catch (SAXException | IOException | ParserConfigurationException | ClassNotFoundException ex) {
            throw new FailedToLoadTOCException(ex);
        }
        
        // Adding unreferenced pages to the unorganized node
        for (Page page : pages.values()) {
            
            if (!toc.getRoot().isReferenced(page)) { 
                toc.getUnorganized().addToEnd(
                        new TOCNode(page));
            }
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
     */
    public void reload() throws FailedToLoadPageException, FailedToLoadTOCException {    
        
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
     *                                    same identifier
     * @throws IOException
     * @see Page
     */
    public void addNewPage(Page page) throws PageAlreadyExistsException, IOException {
        
        String id = page.getId();
        if (!pages.containsKey(id)) {
            registerPage(page);
        }
        else {
            throw new PageAlreadyExistsException();
        }
        
        if (!toc.getReferencedPages().contains(id)) {
            toc.addToEnd(toc.getUnorganized(), new TOCNode(page));
        }                
        
        File[] pageFiles = page.save(getDocumentationDirectory());
        
        for (File pageFile : pageFiles) {
            log.info("Adding new file to repository: " + pageFile.getName());
        }
        
        AddCommand cmd = new AddCommand(repository);
        cmd.execute(pageFiles);
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
     * This method only modifies the tracked files, but does not invoke 
     * commit on the repository!
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
        }
        catch (IOException | TransformerException ex) {
            log.error(null, ex);
            
            throw new CouldNotSaveDocumentationException(ex);        
        }    
    }
    
    /**
     * Checks if anything has changed in the documentation
     * 
     * @return returns true if any file has been added, removed or modified in
     *         the documentation's repository
     */
    public boolean hasChanges() {
                
        StatusCommand status = new StatusCommand(repository);
        StatusResult result = status.execute();

        return result.getAdded().size() > 0 ||
                result.getCopied().size() > 0 ||
                result.getModified().size() > 0 ||
                result.getRemoved().size() > 0;     
    }
    
    /**
     * Gets the current changes applied on the documentation
     * 
     * @return returns a map where each modified file's relative path is mapped
     *         to the change type
     * @see Change
     */
    public Map<String, Change> getChanges() {

        StatusCommand status = new StatusCommand(repository);
        StatusResult result = status.execute();

        Map<String, Change> changes = new HashMap<>();

        for (String change : result.getAdded()) {
            changes.put(change, Change.Added);
        }

        for (String change : result.getCopied().values()) {
            changes.put(change, Change.Copied);
        }

        for (String change : result.getModified()) {
            changes.put(change, Change.Modified);
        }

        for (String change : result.getRemoved()) {
            changes.put(change, Change.Removed);
        }

        return changes;              
    }
        
    /**
     * Commit the changes as a new changeset
     * 
     * <p>
     * Use the getChanges method to get the list of modified files before
     * calling this method!
     * 
     * @param message the commit message
     * @param files list of files to be committed
     */
    public void commitChanges(String message, List<String> files) {

        CommitCommand commit = new CommitCommand(repository);
        commit.message(message);                        
        commit.user(System.getProperty("user.name"));

        String[] items = Arrays.copyOf(files.toArray(), files.size(), String[].class);
        commit.execute(items);
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
     */
    public void revertChanges(List<String> files) throws FailedToLoadPageException, FailedToLoadTOCException {        
        
        RevertCommand revert = new RevertCommand(repository);

        String[] items = Arrays.copyOf(files.toArray(), files.size(), String[].class);
        revert.execute(items);        
        
        reload();
    }    

    /**
     * Gets the repository's root directory.
     * 
     * <p>
     * This is not always the same as the documentation's root directory!
     * Documentations stored in a subdirectory of a repository are also supported.
     
     * @return returns the absolute path of the repository
     */
    public String getRepositoryRoot() {
        return getDocumentationDirectory().getAbsolutePath();
    }

    @Override
    public void update(Observable o, Object o1) {
        
        if (o instanceof Snippet) {
            
            Snippet snippet = (Snippet)o;
            
            for (Page page : pages.values()) {
                
                if (page.referencesSnippet(snippet)) {
                    page.refresh();
                }
            }
        }
        else if (o instanceof Page) {
            
            Page page = (Page)o;
            
            for (String pageId : page.getReferencedPages()) {
                                
                if (!pages.containsKey(pageId)) {
                    
                    Page newPage = new Page(pageId, this);
                    
                    try {
                        addNewPage(newPage);
                    }
                    catch (IOException | PageAlreadyExistsException ex) {
                        log.error(null, ex);
                    }
                } else {
                
                    Page existingPage = pages.get(pageId);
                    if (toc.getRecycleBin().isReferenced(existingPage)) {
                        
                        // If a reference has been created to a page which is in the 
                        // recycle bin, we move it to the unorganized pages node
                        toc.getRecycleBin().removeReferenceTo(existingPage);
                        toc.addToEnd(toc.getUnorganized(), new TOCNode(existingPage));
                        
                    }
                }
            }         
        }               
    }
    
    /**
     * Looks for orphaned pages and moves them to the recycle bin node in the
     * TOC. Pages which has not been changed from the original template are 
     * immediately deleted and removed from the repository.
     */
    public void processOrphanedPages() {
        
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
                if (toc.getRecycleBin().findReferenceTo(page) == null) {
                
                    // ..if not, we remove it from wherever it is and put it there
                    log.info(" -> putting it to recycle bin");                               

                    toc.remove(page);
                    toc.addToEnd(toc.getRecycleBin(), new TOCNode(page));
                }
            } else {
                
                log.info(" -> was not modified, removing it");
                
                // ..otherwise we don't keep reference to it in the TOC and
                // delete it from the repository as well                  
                toc.remove(page);
                pages.remove(pageId);
                
                RemoveCommand remove = new RemoveCommand(repository).force();
                
                File[] files = page.getFiles(getDocumentationDirectory());
                remove.execute(files);
                
                for (File f : files) {
                    boolean deleteSucceeded = f.delete();
                    if (!deleteSucceeded) {
                        log.error("Failed to delete file " + f.getName());
                    }
                }
            }                   
        }
        
        log.info("Finished processing orphaned pages.");
    }

    /**
     * Pulls changesets from a remote repository
     * 
     * @param source URL or path to the remote repository
     * @return returns the list of changesets pulled from the remote location
     * @throws IOException
     */
    public List<Changeset> pull(String source) throws IOException {
        PullCommand cmd = new PullCommand(repository).insecure();
        return cmd.execute(source);
    }
    
    /**
     * Pushes the local changesets to a remote repository
     * 
     * @param destination URL or path to the remote repository
     * @return returns the list of changesets pushed to the remote location
     * @throws IOException
     */
    public List<Changeset> push(String destination) throws IOException {
        PushCommand cmd = new PushCommand(repository).insecure();
        return cmd.execute(destination);
    }

    private File findRealRepositoryRoot(File repositoryRoot) {
        boolean found = false;
        
        if (repositoryRoot.isDirectory()) {
            
            File hgdir = new File(repositoryRoot, ".hg");
            if (hgdir.exists() &&
                hgdir.isDirectory()) {
                found = true;
            }
        }            
        
        if (found) {
            return repositoryRoot;
        }
        else {
            File parent = repositoryRoot.getParentFile();
            if (parent != null) {
                return findRealRepositoryRoot(parent);
            }
            else {
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
     * @throws PageAlreadyExistsException When a snippet with the same identifier
     *                                    is already added to the documentation
     */
    @Override
    public void addSnippet(Snippet snippet) throws IOException, PageAlreadyExistsException {
        
        String id = snippet.getId();
        if (!snippets.containsKey(id)) {
            registerSnippet(snippet);
        }
        else {
            throw new PageAlreadyExistsException();
        }        
        
        File[] snippetFiles = snippet.save(getSnippetsDirectory());
        
        for (File snippetFile : snippetFiles) {
            log.info("Adding new snippet to repository: " + snippetFile.getName());
        }
        
        AddCommand cmd = new AddCommand(repository);
        cmd.execute(snippetFiles);
        
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
                
       RemoveCommand remove = new RemoveCommand(repository).force();
       
       File[] files = snippet.getFiles(getSnippetsDirectory());
       remove.execute(files);
                
       for (File f : files) {
        if (!f.delete()) {
               log.error("Failed to delete snippet file " + f.getName());
           }
       }
       
       setChanged();
       notifyObservers();
    }

    private void fixMissingFiles() {
        
        StatusCommand status = new StatusCommand(repository);
        StatusResult result = status.execute();

        List<String> toRemove = new LinkedList<>();
        for (String missing : result.getMissing()) {
            
            File root = new File(getRepositoryRoot());
            File missingFile = new File(root, missing);
            
            if (missingFile.getAbsolutePath().startsWith(getDocumentationDirectory().getAbsolutePath())) {
                log.info("Forgetting missing file " + missing);                                
                toRemove.add(missing);
            }
            else {
                log.info("Leaving missing file " + missing);
            }
        }
        
        if (!toRemove.isEmpty()) {
            RemoveCommand remove = new RemoveCommand(repository).after().force();

            remove.execute(toRemove.toArray(new String[0]));
        }
    }

    private void ensurePageFilesAdded(Page page, File root) {
        
        File[] files = page.getFiles(root);
        for (File f : files) {
            if (f.exists()) {
                
                log.debug("Checking status of " + f.getName());
                
                StatusCommand status = new StatusCommand(repository);
                StatusResult result = status.execute(f);
                
                if (result.getUnknown().size() > 0) {
                    
                    log.debug(" -> status is unknown, adding to repository...");
                    
                    AddCommand add = new AddCommand(repository);
                    add.execute(f);                    
                }
            }
        }
    }

    /**
     * Gets the color associated with given status values
     * 
     * @param status the status string queried
     * @return returns a color which can be used as background color representing
     *         the queried status. The default is white.
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
        }
        else {
            return Color.WHITE;
        }
    }

    /**
     * Changes the given page's identifier and modifies every other page that
     * refers to it.
     * @param page Page to be changed
     * @param newId New identifier of the page
     */
    public void renamePage(Page page, String newId) throws CouldNotSaveDocumentationException, FailedToLoadPageException, FailedToLoadTOCException {                
        
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
                RenameCommand rename = new RenameCommand(repository);
                rename.force();                
                rename.execute(pageFile, new File(pageFile.toString().replace(page.getId(), newId)));                
                
                log.debug("Rename return code " + rename.getReturnCode() + ", error message: " + rename.getErrorString());
            }
            
            // Reload everything
            reload();                   
        }
    }
}
