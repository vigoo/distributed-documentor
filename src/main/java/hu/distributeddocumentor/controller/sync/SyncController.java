package hu.distributeddocumentor.controller.sync;

import hu.distributeddocumentor.gui.PageEditorHost;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.FailedToLoadMetadataException;
import hu.distributeddocumentor.model.FailedToLoadPageException;
import hu.distributeddocumentor.model.FailedToLoadTOCException;
import java.io.IOException;
import java.net.URI;

public class SyncController {

    private final RepositoryQuery query;
    private final RepositoryMerger merger;
    private final RepositorySynchronizer syncer;
    private final SyncInteraction interaction;
    private final Documentation doc;
    private final PageEditorHost host;

    public SyncController(RepositoryQuery query, RepositoryMerger merger, RepositorySynchronizer syncer, SyncInteraction interaction, Documentation doc, PageEditorHost host) {
        this.query = query;
        this.merger = merger;
        this.syncer = syncer;
        this.interaction = interaction;
        this.doc = doc;
        this.host = host;
    }

    public boolean push() throws IOException, FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {
        return run(true);
    }

    public boolean pull() throws IOException, FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {
        return run(false);
    }

    private boolean run(boolean pushing) throws IOException, FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {

        boolean cancelled = false;
        while (query.hasUncommittedChanges() && !cancelled) {

            CommitOrRevert response = interaction.askCommitOrRevert();

            switch (response) {
                case Commit:
                    cancelled = !interaction.showCommitDialog();
                    break;
                case Revert:
                    cancelled = !interaction.showRevertDialog();
                    break;
                case Cancel:
                    cancelled = true;
                    break;
            }
        }

        URI remoteURI = interaction.askURI(query.getDefaultURI());

        cancelled = cancelled
                || remoteURI == null
                || (pushing && !query.hasOutgoingChangesets(remoteURI));

        if (!cancelled) {

            if (query.hasIncomingChangesets(remoteURI)) {
                cancelled = !interaction.showPullDialog(query, syncer, remoteURI);
            }
        }

        if (!cancelled) {

            if (query.requiresMerge()) {
                if (interaction.askMerge()) {

                    merger.mergeAndCommit();
                } else {
                    cancelled = true;
                }
            } else {
                syncer.update();
            }
        }

        if (pushing) {
            cancelled = !interaction.showPushDialog(query, syncer, remoteURI);
        }

        if (!cancelled) {
            doc.reload();
            host.documentationReloaded();
        }

        return !cancelled;
    }
}
