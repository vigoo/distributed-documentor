package hu.distributeddocumentor.controller.sync;

import java.io.IOException;

public interface RepositoryMerger {

    void mergeAndCommit() throws IOException;
}
