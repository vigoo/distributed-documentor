package hu.distributeddocumentor.controller.sync;

import com.aragost.javahg.Bundle;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.*;
import com.aragost.javahg.merge.MergeContext;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import hu.distributeddocumentor.gui.LongOperationRunner;
import hu.distributeddocumentor.gui.ProgressUI;
import hu.distributeddocumentor.gui.RunnableWithProgress;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.vcs.VersionControl;
import hu.distributeddocumentor.vcs.mercurial.MercurialVersionControl;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MercurialSync implements RepositoryQuery, RepositoryMerger, RepositorySynchronizer {

    private static final Logger log = LoggerFactory.getLogger(MercurialSync.class.getName());

    private final Documentation doc;
    private final DocumentorPreferences prefs;
    private final LongOperationRunner longOp;

    private Bundle incomingBundle;
    private List<Changeset> outgoingChangesets;

    /**
     *
     * @param doc Documentation to synchronize
     * @param prefs Application preferences
     * @param longOp Long operation runner interface
     */
    public MercurialSync(Documentation doc, DocumentorPreferences prefs, LongOperationRunner longOp) {
        this.doc = doc;
        this.prefs = prefs;
        this.longOp = longOp;
    }

    @Override
    public boolean hasUncommittedChanges() {
        // TODO: refactor so Documentation has no version control specific code                
        return doc.getVersionControl().hasChanges();
    }

    @Override
    public boolean hasIncomingChangesets(final URI uri) {

        return longOp.run(new Function<ProgressUI, Boolean>() {

            @Override
            public Boolean apply(ProgressUI progress) {

                progress.setIndeterminate();
                progress.setStatus("Checking for incoming change sets...");

                Repository repo = getRepository();
                IncomingCommand incoming = new IncomingCommand(repo).insecure();

                log.debug("Getting incoming change sets...");

                incomingBundle = incoming.execute(uri.toASCIIString());

                if (incomingBundle != null) {
                    List<Changeset> changesets = incomingBundle.getChangesets();

                    log.debug("Got " + changesets.size() + " change sets:");
                    for (Changeset cs : changesets) {
                        log.debug(" - " + cs.toString());
                    }

                    return !changesets.isEmpty();
                } else {
                    log.debug("No change sets found");
                    return false;
                }
            }
        });
    }

    @Override
    public boolean requiresMerge() {

        Repository repo = getRepository();
        HeadsCommand heads = new HeadsCommand(repo);

        List<Changeset> result = heads.execute();
        return result.size() > 1;
    }

    @Override
    public void mergeAndCommit() {

        longOp.run(new RunnableWithProgress() {

            @Override
            public void run(ProgressUI progress) {
                try {
                    progress.setIndeterminate();
                    progress.setStatus("Merging changes...");

                    Repository repo = getRepository();
                    MergeCommand merge = new MergeCommand(repo);
                    MergeContext context = merge.execute();
                    if (context.getMergeConflicts().size() > 0) {
                        try {
                            Process proc = Runtime.getRuntime().exec(
                                    new String[]{prefs.getMercurialPath(), "resolve", "--all"},
                                    null,
                                    new File(doc.getRepositoryRoot()));
                            proc.waitFor();
                        } catch (InterruptedException ex) {
                            log.error(null, ex);
                        }
                    }

                    CommitCommand commit
                            = new CommitCommand(repo)
                            .message("Merged changes")
                            .user(System.getProperty("user.name"));
                    commit.execute();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    public List<RepoChangeSet> incomingChangesets(final URI uri) {

        return longOp.run(new Function<ProgressUI, List<RepoChangeSet>>() {

            @Override
            public List<RepoChangeSet> apply(ProgressUI progress) {
                progress.setIndeterminate();
                progress.setStatus("Getting incoming changesets...");

                if (incomingBundle == null) {
                    if (!hasIncomingChangesets(uri)) {
                        return new LinkedList<>();
                    }
                }

                return Lists.transform(incomingBundle.getChangesets(),
                        new Function<Changeset, RepoChangeSet>() {
                            @Override
                            public RepoChangeSet apply(Changeset input) {
                                return new MercurialChangeSet(input);
                            }
                        });
            }
        });
    }

    @Override

    public boolean hasOutgoingChangesets(final URI uri) {

        return longOp.run(new Function<ProgressUI, Boolean>() {

            @Override
            public Boolean apply(ProgressUI progress) {
                progress.setIndeterminate();
                progress.setStatus("Checking for outgoing changes...");
                
                Repository repo = getRepository();
                OutgoingCommand outgoing = new OutgoingCommand(repo).insecure();

                outgoingChangesets = outgoing.execute(uri.toASCIIString());
                return !outgoingChangesets.isEmpty();
            }
        });
    }

    @Override

    public List<RepoChangeSet> outgoingChangesets(final URI uri) {

        return longOp.run(new Function<ProgressUI, List<RepoChangeSet>>() {

            @Override
            public List<RepoChangeSet> apply(ProgressUI progress) {
                
                progress.setIndeterminate();
                progress.setStatus("Getting outgoing changesets...");

                if (outgoingChangesets == null) {
                    if (!hasOutgoingChangesets(uri)) {
                        return new LinkedList<>();
                    }
                }

                return Lists.transform(outgoingChangesets,
                        new Function<Changeset, RepoChangeSet>() {
                            @Override
                            public RepoChangeSet apply(Changeset input) {
                                return new MercurialChangeSet(input);
                            }
                        });
            }
        });
    }

    @Override
    public void pull(final URI uri) throws IOException {

        longOp.run(new RunnableWithProgress() {

            @Override
            public void run(ProgressUI progress) {
                progress.setIndeterminate();
                progress.setStatus("Pulling changes...");
                
                try {
                    pull(uri.toASCIIString());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    public void push(final URI uri) throws IOException {
        longOp.run(new RunnableWithProgress() {

            @Override
            public void run(ProgressUI progress) {
                progress.setIndeterminate();
                progress.setStatus("Pushing changes...");
                
                try {
                    push(uri.toASCIIString());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    public URI getDefaultURI() {
        File hgrc = new File(getRepository().getDirectory(), ".hg/hgrc");
        try {
            Ini ini = new Ini(hgrc);
            String defaultUri = ini.get("paths", "default");

            return URI.create(defaultUri);
        } catch (IOException ex) {
            log.error(null, ex);
            return null;
        }
    }

    @Override
    public void update() throws IOException {
        longOp.run(new RunnableWithProgress() {

            @Override
            public void run(ProgressUI progress) {
                progress.setIndeterminate();
                progress.setStatus("Updating files...");
                
                try {                                        
                    UpdateCommand update = new UpdateCommand(getRepository());
                    update.execute();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
    
      /**
     * Pulls changesets from a remote repository
     *
     * @param source URL or path to the remote repository
     * @return returns the list of changesets pulled from the remote location
     * @throws IOException
     */    
    private List<Changeset> pull(String source) throws IOException {
        PullCommand cmd = new PullCommand(getRepository()).insecure();
        return cmd.execute(source);
    }

    /**
     * Pushes the local changesets to a remote repository
     *
     * @param destination URL or path to the remote repository
     * @return returns the list of changesets pushed to the remote location
     * @throws IOException
     */    
    private List<Changeset> push(String destination) throws IOException {
        PushCommand cmd = new PushCommand(getRepository()).insecure();
        return cmd.execute(destination);
    }


    private Repository getRepository() {
        VersionControl versionControl = doc.getVersionControl();
        if (versionControl instanceof MercurialVersionControl) {
            return ((MercurialVersionControl)versionControl).getRepository();                
        }
        else {
            throw new IllegalStateException("MercurialSync can only be used with MercurialVersionControl");
        }
    }
}
