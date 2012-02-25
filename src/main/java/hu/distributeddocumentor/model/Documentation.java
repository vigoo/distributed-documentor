/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.distributeddocumentor.model;

import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.*;
import hu.distributeddocumentor.utils.RepositoryUriGenerator;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author vigoo
 */
public class Documentation implements Observer {
    
    private final TOC toc;
    private final Map<String, Page> pages;    
    private Images images;
    
    private Repository repository;

    public TOC getTOC() {
        return toc;
    }        

    public Documentation() {
        
        toc = new TOC();
        pages = new HashMap<String, Page>();                   
    }
    
    public void initAsNew(File repositoryRoot) throws IOException {
        
        repository = Repository.create(repositoryRoot);
        images = new Images(repository);
        
        Page first = new Page("start");
        
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
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
            
            throw new IllegalStateException("Must be called on a fresh instance!");
        } catch (TransformerException ex) {
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);                        
            
            throw new IllegalStateException("Must be called on a fresh instance!");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
            
            throw new IllegalStateException("Must be called on a fresh instance!");
        }
    }   
    
    public void initFromExisting(File repositoryRoot) throws SAXException, IOException, ParserConfigurationException {
        
        repository = Repository.open(repositoryRoot);
        images = new Images(repository);
        
        loadRepository();
    }
    
    public void cloneFromRemote(File localRepositoryRoot, String remoteRepo, String userName, String password) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
                
        repository = Repository.clone(localRepositoryRoot, RepositoryUriGenerator.addCredentials(remoteRepo, userName, password));
        images = new Images(repository);
        
        loadRepository();
    }
    
    private void loadRepository() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {

        for (File child : repository.getDirectory().listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() &&
                               !"toc.xml".equals(file.getName()) &&                               
                               !".DS_Store".equals(file.getName()) && 
                               !file.getName().endsWith(".orig");
                    }})) {
            
            Page page = new Page(child);
            registerPage(page);
        }
        
        toc.load(repository.getDirectory(), this);
        
        // Adding unreferenced pages to the unorganized node
        for (Page page : pages.values()) {
            
            if (!toc.getRoot().isReferenced(page)) 
                toc.getUnorganized().addToEnd(
                        new TOCNode(page));
        }                
    }
    
    public void reload() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {    
        
        toc.clear();
        pages.clear();
        images.reload();
        
        loadRepository();
    }
    
    public void addNewPage(Page page) throws PageAlreadyExistsException, IOException {
        
        String id = page.getId();
        if (!pages.containsKey(id))
            registerPage(page);
        else
            throw new PageAlreadyExistsException();
        
        toc.addToEnd(toc.getUnorganized(), new TOCNode(page));
        
        File pageFile = page.save(repository.getDirectory());
        AddCommand cmd = new AddCommand(repository);
        cmd.execute(pageFile);
    }      
    
    private void registerPage(Page page) {
        pages.put(page.getId(), page);
     
        page.addObserver(this);
    }
    
    public Page getPage(String id) {
        return pages.get(id);
    }
    
    public Images getImages() {
        return images;
    }
    
    public void saveAll() throws CouldNotSaveDocumentationException {
        
        File root = repository.getDirectory();
        
        try {
            for (Page page : pages.values()) {
                page.saveIfModified(root);
            }        
        
            toc.save(root);
        }
        catch (IOException ex) {
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
            
            throw new CouldNotSaveDocumentationException(ex);        
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
            
            throw new CouldNotSaveDocumentationException(ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
            
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
    
    public void revertChanges(List<String> files) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {        
        
        RevertCommand revert = new RevertCommand(repository);

        String[] items = Arrays.copyOf(files.toArray(), files.size(), String[].class);
        revert.execute(items);        
        
        reload();
    }    

    public String getRepositoryRoot() {
        return repository.getDirectory().getAbsolutePath();
    }

    @Override
    public void update(Observable o, Object o1) {
        
        if (o instanceof Page) {
            
            Page page = (Page)o;
            
            for (String pageId : page.getReferencedPages()) {
                
                if (!pages.containsKey(pageId)) {
                    
                    Page newPage = new Page(pageId);
                    
                    try {
                        addNewPage(newPage);
                    }
                    catch (IOException ex) {
                        Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    catch (PageAlreadyExistsException ex) {
                        // This cannot happen
                        Logger.getLogger(Documentation.class.getName()).log(Level.SEVERE, null, ex);
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
        
        // Collect all references
        Set<String> referencedPages = new HashSet<String>();
        
        referencedPages.add("start");
        for (Page page : pages.values()) {
            referencedPages.addAll(page.getReferencedPages());
        }
        
        // Collect the pages which are NOT referenced
        Set<String> orphanedPages = new HashSet<String>(pages.keySet());
        orphanedPages.removeAll(referencedPages);
        
        for (String pageId : orphanedPages) {
            
            Page page = pages.get(pageId);
            
            toc.remove(page);
            
            // If the page does not equals the default template
            if (!page.equalsTemplate()) {                      
                // ..then we don't delete it, but put into the recycle bin
                // node instead of the unorganized pages node
                toc.addToEnd(toc.getRecycleBin(), new TOCNode(page));
            } else {
                // ..otherwise we don't keep reference to it in the TOC and
                // delete it from the repository as well                                           
                pages.remove(pageId);
                
                RemoveCommand remove = new RemoveCommand(repository);
                remove.execute(page.getFile(repository.getDirectory()));
                
                page.getFile(repository.getDirectory()).delete();
            }                   
        }
    }

}
