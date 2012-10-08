package hu.distributeddocumentor.controller.sync;

import java.net.URI;
import java.util.List;

public interface RepositoryQuery {

    URI getDefaultURI();
            
    boolean hasUncommittedChanges();
    boolean hasIncomingChangesets(URI uri);
    List<RepoChangeSet> incomingChangesets(URI uri);
    boolean hasOutgoingChangesets(URI uri);
    List<RepoChangeSet> outgoingChangesets(URI uri);
    
    boolean requiresMerge();   
}
