package hu.distributeddocumentor.controller.sync;

import java.io.IOException;

public interface RepositorySynchronizer {

    void pull() throws IOException;
    void push() throws IOException;
    
}
