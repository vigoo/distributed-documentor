package hu.distributeddocumentor.controller.sync;

import com.aragost.javahg.Changeset;

/**
 * Wraps the JavaHG's changeset class behind the {@link RepoChangeSet} interface
 * @author Daniel Vigovszky
 */
public class MercurialChangeSet implements RepoChangeSet {
    
    private final Changeset changeset;

    public MercurialChangeSet(Changeset changeset) {
        this.changeset = changeset;
    }        

    @Override
    public String getRevision() {
        return Integer.toString(changeset.getRevision());
    }

    @Override
    public String getUser() {
        return changeset.getUser();
    }

    @Override
    public String getTimestamp() {
        return changeset.getTimestamp().getDate().toLocaleString();
    }

    @Override
    public String getMessage() {
        return changeset.getMessage();
    }
}
