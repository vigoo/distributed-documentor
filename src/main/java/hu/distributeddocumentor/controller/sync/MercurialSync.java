package hu.distributeddocumentor.controller.sync;

import com.aragost.javahg.Bundle;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.*;
import com.aragost.javahg.merge.MergeContext;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
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
    
    private Bundle incomingBundle;    
    private List<Changeset> outgoingChangesets;

    public MercurialSync(Documentation doc, DocumentorPreferences prefs) {
        this.doc = doc;
        this.prefs = prefs;
    }
        
    
    @Override
    public boolean hasUncommittedChanges() {
        
        // TODO: refactor so Documentation has no version control specific code
        return doc.hasChanges();
    }

    @Override
    public boolean hasIncomingChangesets(URI uri) {
        
        Repository repo = doc.getRepository();
        IncomingCommand incoming = new IncomingCommand(repo).insecure();
        
        log.debug("Getting incoming change sets...");
        
        incomingBundle = incoming.execute(uri.toASCIIString());
        
        if (incomingBundle != null) {
            List<Changeset> changesets = incomingBundle.getChangesets();

            log.debug("Got {0} change sets:", changesets.size());
            for (Changeset cs : changesets) {
                log.debug(" - {0}", cs.toString());
            }

            return !changesets.isEmpty();
        }
        else {
            log.debug("No change sets found");
            return false;
        }
    }

    @Override
    public boolean requiresMerge() {
        
        Repository repo = doc.getRepository();
        HeadsCommand heads = new HeadsCommand(repo);
        
        List<Changeset> result = heads.execute();
        return result.size() > 1;        
    }

    @Override
    public void mergeAndCommit() throws IOException {
        
        Repository repo = doc.getRepository();
        MergeCommand merge = new MergeCommand(repo);
        MergeContext context = merge.execute();
        if (context.getMergeConflicts().size() > 0) {
            try {
                Process proc = Runtime.getRuntime().exec(
                        new String[] { prefs.getMercurialPath(), "resolve", "--all" },
                        null, 
                        new File(doc.getRepositoryRoot()));
                proc.waitFor();
            } catch (InterruptedException ex) {                
                log.error(null, ex);
            }
        }        
        
        CommitCommand commit = 
                new CommitCommand(repo)
                .message("Merged changes")
                .user(System.getProperty("user.name"));
        commit.execute();
    }	

    @Override
    public List<Changeset> incomingChangesets(URI uri) {
        
        if (incomingBundle == null)
            if (!hasIncomingChangesets(uri))
                return new LinkedList<Changeset>();
        
        return incomingBundle.getChangesets();
    }

    @Override
    public boolean hasOutgoingChangesets(URI uri) {
        
        Repository repo = doc.getRepository();
        OutgoingCommand outgoing = new OutgoingCommand(repo).insecure();
        
        outgoingChangesets = outgoing.execute(uri.toASCIIString());
        return !outgoingChangesets.isEmpty();
        
    }

    @Override
    public List<Changeset> outgoingChangesets(URI uri) {
        if (outgoingChangesets == null)
            if (!hasOutgoingChangesets(uri))
                return new LinkedList<Changeset>();
        
        return outgoingChangesets;
    }

    @Override
    public void pull(URI uri) throws IOException {
        
        doc.pull(uri.toASCIIString());
    }

    @Override
    public void push(URI uri) throws IOException {
        
        doc.push(uri.toASCIIString());
    }

    @Override
    public URI getDefaultURI() {
        File hgrc = new File(doc.getRepository().getDirectory(), ".hg/hgrc");        
        try {
            Ini ini = new Ini(hgrc);
            String defaultUri = ini.get("paths", "default");
            
            return URI.create(defaultUri);
        }
        catch (Exception ex) {
            log.error(null, ex);
            return null;
        }
    }

    @Override
    public void update() throws IOException {
        UpdateCommand update = new UpdateCommand(doc.getRepository());
        update.execute();
    }

}
