package hu.distributeddocumentor.controller.sync;

import com.aragost.javahg.Bundle;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.HttpAuthorizationRequiredException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ini4j.Ini;


public class MercurialSync implements RepositoryQuery, RepositoryMerger, RepositorySynchronizer {

    private static final Logger logger = Logger.getLogger(MercurialSync.class.getName());
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
    public boolean hasIncomingChangesets(URI uri) throws HttpAuthorizationRequiredException {
        
        Repository repo = doc.getRepository();
        IncomingCommand incoming = new IncomingCommand(repo).insecure();
        
        logger.log(Level.FINE, "Getting incoming change sets...");
        
        incomingBundle = incoming.execute(uri.toASCIIString());
        
        if (incomingBundle != null) {
            List<Changeset> changesets = incomingBundle.getChangesets();

            logger.log(Level.FINE, "Got {0} change sets:", changesets.size());
            for (Changeset cs : changesets) {
                logger.log(Level.FINE, " - {0}", cs.toString());
            }

            return changesets.size() > 0;
        }
        else {
            logger.log(Level.FINE, "No change sets found");
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
                logger.log(Level.SEVERE, null, ex);
            }
        }        
        
        CommitCommand commit = 
                new CommitCommand(repo)
                .message("Merged changes")
                .user(System.getProperty("user.name"));
        commit.execute();
    }	

    @Override
    public List<Changeset> incomingChangesets(URI uri) throws HttpAuthorizationRequiredException {
        
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
        return outgoingChangesets.size() > 0;
        
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
            logger.severe(ex.toString());
            return null;
        }
    }

}
