package hu.distributeddocumentor.controller.sync;

public interface SyncInteraction {

    CommitOrRevert askCommitOrRevert();
    boolean showCommitDialog();
    boolean showRevertDialog();
    boolean showPullDialog(RepositoryQuery query, RepositorySynchronizer syncer);
    boolean askMerge();
    boolean showPushDialog(RepositoryQuery query, RepositorySynchronizer syncer);
}
