package hu.distributeddocumentor.model;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.RepositoryConfiguration;
import com.aragost.javahg.commands.*;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.utils.CaseInsensitiveMap;
import hu.distributeddocumentor.utils.RepositoryUriGenerator;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

public class Documentation implements Observer, SnippetCollection {
    
    private static final Logger logger = Logger.getLogger(Documentation.class.getName());
    
    private final TOC toc;
    private final Map<String, Page> pages;    
    private final Map<String, Snippet> snippets;
    private Images images;
    
    private String relativeRoot;
    private Repository repository;
    private final DocumentorPreferences prefs;

    public TOC getTOC() {
        return toc;
    }

    public Repository getRepository() {
        return repository;
    }        

    public Documentation(DocumentorPreferences prefs) {
        
        toc = new TOC();
        pages = new CaseInsensitiveMap<Page>();
        snippets = new CaseInsensitiveMap<Snippet>();
        
        this.prefs = prefs;
    }
    
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
        } catch (TransformerConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            
            throw new IllegalStateException("Must be called on a fresh instance!");
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);                        
            
            throw new IllegalStateException("Must be called on a fresh instance!");
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
            
            throw new IllegalStateException("Must be called on a fresh instance!");
        }
        
        File snippetsDir = getSnippetsDirectory();
        if (!snippetsDir.exists())
            snippetsDir.mkdirs();
    }   
    
    public void initFromExisting(File repositoryRoot) throws FailedToLoadPageException, FailedToLoadTOCException {
        
        File realRepositoryRoot = findRealRepositoryRoot(repositoryRoot);
        relativeRoot = realRepositoryRoot.toURI().relativize(repositoryRoot.toURI()).getPath();
        
        logger.log(Level.INFO, "Specified root: {0}", repositoryRoot.toString());
        logger.log(Level.INFO, "Real root:      {0}", realRepositoryRoot.toString());
        logger.log(Level.INFO, "Relative root:  {0}", relativeRoot);
        
        repository = Repository.open(createRepositoryConfiguration(), realRepositoryRoot);
        images = new Images(repository, relativeRoot);
        
        loadRepository();
    }
    
    public void cloneFromRemote(File localRepositoryRoot, String remoteRepo, String userName, String password) throws FailedToLoadPageException, FailedToLoadTOCException {
                
        relativeRoot = "";
        repository = Repository.clone(createRepositoryConfiguration(), localRepositoryRoot, RepositoryUriGenerator.addCredentials(remoteRepo, userName, password));
        images = new Images(repository, relativeRoot);
        
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
        if (!snippetsDir.exists())
            snippetsDir.mkdirs();
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
        catch (Exception ex) {
            throw new FailedToLoadTOCException(ex);
        }
        
        // Adding unreferenced pages to the unorganized node
        for (Page page : pages.values()) {
            
            if (!toc.getRoot().isReferenced(page)) 
                toc.getUnorganized().addToEnd(
                        new TOCNode(page));
        }                
    }
    
    public void reload() throws FailedToLoadPageException, FailedToLoadTOCException {    
        
        toc.clear();
        pages.clear();
        images.reload();
        
        loadRepository();
    }
    
    private boolean isSupportedMarkup(File file) {
        return file.getName().toLowerCase().endsWith(".mediawiki");
    }
    
    public void addNewPage(Page page) throws PageAlreadyExistsException, IOException {
        
        String id = page.getId();
        if (!pages.containsKey(id))
            registerPage(page);
        else
            throw new PageAlreadyExistsException();
        
        if (!toc.getReferencedPages().contains(id))
            toc.addToEnd(toc.getUnorganized(), new TOCNode(page));                
        
        File pageFile = page.save(getDocumentationDirectory());
        
        logger.log(Level.INFO, "Adding new file to repository: {0}", pageFile.getName());
        
        AddCommand cmd = new AddCommand(repository);
        cmd.execute(pageFile);
    }      
    
    private void registerPage(Page page) {
        pages.put(page.getId(), page);
     
        page.addObserver(this);
    }
    
    private void registerSnippet(Snippet snippet) {
        snippets.put(snippet.getId(), snippet);
        
        snippet.addObserver(this);
    }
    
    public Page getPage(String id) {
        return pages.get(id);
    }
    
    public Images getImages() {
        return images;
    }
    
    public void saveAll() throws CouldNotSaveDocumentationException {
        
        File root = getDocumentationDirectory();
        
        try {
            for (Page page : pages.values()) {
                page.saveIfModified(root);
            }        
            
            File snippetsDir = getSnippetsDirectory();
            for (Snippet snippet : snippets.values()) {
                snippet.saveIfModified(snippetsDir);
            }
        
            toc.saveIfModified(root);
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            
            throw new CouldNotSaveDocumentationException(ex);        
        } catch (TransformerConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            
            throw new CouldNotSaveDocumentationException(ex);
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
            
            throw new CouldNotSaveDocumentationException(ex);
        }    
    }
    
    public boolean hasChanges() {
                
        StatusCommand status = new StatusCommand(repository);
        StatusResult result = status.execute();

        return result.getAdded().size() > 0 ||
                result.getCopied().size() > 0 ||
                result.getModified().size() > 0 ||
                result.getRemoved().size() > 0;     
    }
    
    public Map<String, Change> getChanges() {

        StatusCommand status = new StatusCommand(repository);
        StatusResult result = status.execute();

        Map<String, Change> changes = new HashMap<String, Change>();

        for (String change : result.getAdded())
            changes.put(change, Change.Added);

        for (String change : result.getCopied().values())
            changes.put(change, Change.Copied);

        for (String change : result.getModified())
            changes.put(change, Change.Modified);

        for (String change : result.getRemoved())
            changes.put(change, Change.Removed);

        return changes;              
    }
        
    public void commitChanges(String message, List<String> files) {

        CommitCommand commit = new CommitCommand(repository);
        commit.message(message);                        
        commit.user(System.getProperty("user.name"));

        String[] items = Arrays.copyOf(files.toArray(), files.size(), String[].class);
        commit.execute(items);
    }
    
    public void revertChanges(List<String> files) throws FailedToLoadPageException, FailedToLoadTOCException {        
        
        RevertCommand revert = new RevertCommand(repository);

        String[] items = Arrays.copyOf(files.toArray(), files.size(), String[].class);
        revert.execute(items);        
        
        reload();
    }    

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
                    catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    catch (PageAlreadyExistsException ex) {
                        // This cannot happen
                        logger.log(Level.SEVERE, null, ex);
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
    
    public void processOrphanedPages() {
        
        logger.info("Processing orphaned pages...");
        
        // Collect all references
        Set<String> referencedPages = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        
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
        Set<String> orphanedPages = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        orphanedPages.addAll(pages.keySet());        
        orphanedPages.removeAll(referencedPages);
        
        for (String pageId : orphanedPages) {
            
            logger.log(Level.INFO, "Found orphaned page: {0}", pageId);
            
            Page page = pages.get(pageId);
            
            toc.remove(page);
            
            // If the page does not equals the default template
            if (!page.equalsTemplate()) {                      
                
                logger.info(" -> putting it to recycle bin");
                
                // ..then we don't delete it, but put into the recycle bin
                // node instead of the unorganized pages node
                toc.addToEnd(toc.getRecycleBin(), new TOCNode(page));
            } else {
                
                logger.info(" -> was not modified, removing it");
                
                // ..otherwise we don't keep reference to it in the TOC and
                // delete it from the repository as well                                           
                pages.remove(pageId);
                
                RemoveCommand remove = new RemoveCommand(repository);
                remove.execute(page.getFile(repository.getDirectory()));
                
                page.getFile(repository.getDirectory()).delete();
            }                   
        }
        
        logger.info("Finished processing orphaned pages.");
    }

    public List<Changeset> pull() throws IOException {
        PullCommand cmd = new PullCommand(repository);
        return cmd.execute();
    }
    
    public List<Changeset> push() throws IOException {
        PushCommand cmd = new PushCommand(repository);
        return cmd.execute();
    }

    private File findRealRepositoryRoot(File repositoryRoot) {
        boolean found = false;
        
        if (repositoryRoot.isDirectory()) {
            
            File hgdir = new File(repositoryRoot, ".hg");
            if (hgdir.exists() &&
                hgdir.isDirectory())
                found = true;
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

    @Override
    public Snippet getSnippet(String id) {
        return snippets.get(id);
    }

}
