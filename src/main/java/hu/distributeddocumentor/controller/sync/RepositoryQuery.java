package hu.distributeddocumentor.controller.sync;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.HttpAuthorizationRequiredException;
import java.net.URI;
import java.util.List;

public interface RepositoryQuery {

    URI getDefaultURI();
            
    boolean hasUncommittedChanges();
    boolean hasIncomingChangesets(URI uri) throws HttpAuthorizationRequiredException;
    List<Changeset> incomingChangesets(URI uri) throws HttpAuthorizationRequiredException; // TODO: make it mercurial-independent
    boolean hasOutgoingChangesets(URI uri);
    List<Changeset> outgoingChangesets(URI uri); // TODO: make it mercurial-independent
    
    boolean requiresMerge();   
}
