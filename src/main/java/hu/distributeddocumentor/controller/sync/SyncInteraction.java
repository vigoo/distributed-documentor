package hu.distributeddocumentor.controller.sync;

import java.net.URI;

public interface SyncInteraction {

    CommitOrRevert askCommitOrRevert();    
    boolean showCommitDialog();
    boolean showRevertDialog();
    boolean showPullDialog(RepositoryQuery query, RepositorySynchronizer syncer, URI uri);
    boolean askMerge();
    boolean showPushDialog(RepositoryQuery query, RepositorySynchronizer syncer, URI uri);
    URI askURI(URI defaultURI);    
}
