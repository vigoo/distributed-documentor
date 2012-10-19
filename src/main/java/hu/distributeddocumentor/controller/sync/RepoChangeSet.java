package hu.distributeddocumentor.controller.sync;

/**
 * Represents a repository changeset in a version-control system independent
 * way.
 * @author Daniel Vigovszky
 */
public interface RepoChangeSet {

    /**
     * Gets the revision identifier of this changeset
     * @return returns the revision id in string format
     */
    public String getRevision();

    /**
     * Gets the user name of the changeset's author
     * @return the user who committed the changeset
     */
    public String getUser();

    /**
     * Gets the time when the changeset has been committed
     * @return the changeset's commit time in string format
     */
    public String getTimestamp();

    /**
     * Gets the changeset's commit message
     * @return the commit message
     */
    public String getMessage();
    
}
