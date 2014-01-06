
package hu.distributeddocumentor.vcs;

import hu.distributeddocumentor.model.Change;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Abstract version control interface, containing only the operations required
 * by the Documentation model.
 *
 * @author Daniel Vigovszky
 */
public interface VersionControl {
        
    /**
     * Creates a new repository at the given root
     * 
     * @param repositoryRoot root of the repository
     */
    public void create(File repositoryRoot);
    
    /**
     * Opens a repository from the given root
     * @param repositoryRoot root of the repository
     */
    public void open(File repositoryRoot);
    
    /**
     * Clones a remote repository to a given root
     * @param localRepositoryRoot local repository root as a target
     * @param uri uri of the remote repository
     */
    public void clone(File localRepositoryRoot, String uri);

    /**
     * Gets the repository root 
     * @return returns the root directory of the repository
     */
    public File getRoot();    
    
    /**
     * Adds a single file to the repository
     * @param item file which lies under the repository root
     */
    public void add(File item);
    
    /**
     * Adds multiple files to the repository
     * @param items files which lie under the repository root
     */
    public void add(File[] items);
    
    /**
     * Removes a single file from the repository
     * @param item file lying under the repository root
     * @param force remove even if added or modified
     */
    public void remove(File item, boolean force);
    
    /**
     * Removes multiple files from the repository
     * @param items files lying under the repository root
     * @param force remove even if added or modified
     * @param after remove even if they are already deleted
     */
    public void remove(File[] items, boolean force, boolean after);
            
    /**
     * Removes multiple files from the repository
     * @param items repository-relative file names
     * @param force remove even if added or modified
     * @param after remove even if they are already deleted
     */
    public void remove(String[] items, boolean force, boolean after);
    
    /**
     * Reverts changes to a set of files
     * @param items repository-relative file names
     */
    public void revert(String[] items);
    
    
    /**
     * Checks if anything has changed in the documentation
     *
     * @return returns true if any file has been added, removed or modified in
     * the documentation's repository
     */
    public boolean hasChanges();
        
    /**
     * Gets the current changes applied on the documentation
     *
     * @return returns a map where each modified file's relative path is mapped
     * to the change type
     * @see Change
     */
    public Map<String, Change> getChanges();
    
        
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
    public void commitChanges(String message, List<String> files);

    /**
     * Renames a tracked file
     * @param source source file 
     * @param target target file
     * @param force forcibly copy over an existing managed file
     */
    public void rename(File source, File target, boolean force);
   
    /**
     * Gets the repository-relative paths of missing files
     * @return returns an iterable collection of missing files
     */
    public Iterable<String> getMissingFiles();
    
    /**
     * Checks whether a file is tracked or not
     * @param item file lying under the repository root
     * @return true if the file is added to the repository
     */
    public boolean isAdded(File item);
}
