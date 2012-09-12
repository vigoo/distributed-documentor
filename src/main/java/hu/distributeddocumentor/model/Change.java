package hu.distributeddocumentor.model;

/**
 * Possible actions performed on files in a change set
 *
 * @author Daniel Vigovszky
 */
public enum Change {
    /**
     * The file has been added to the repository
     */
    Added,
    /**
     * The file has been removed from the repository
     */
    Removed,
    /**
     * The file was already in the repository and was modified
     */
    Modified,
    /**
     * The file has been copied inside the repository
     */
    Copied
    
}
