package hu.distributeddocumentor.controller.sync;

import com.aragost.javahg.Changeset;
import java.util.List;

public interface RepositoryQuery {

    boolean hasUncommittedChanges();
    boolean hasIncomingChangesets();
    List<Changeset> incomingChangesets(); // TODO: make it mercurial-independent
    boolean hasOutgoingChangesets();
    List<Changeset> outgoingChangesets(); // TODO: make it mercurial-independent
    
    boolean requiresMerge();   
}
