package hu.distributeddocumentor.controller.sync;

import java.io.IOException;
import java.net.URI;

public interface RepositorySynchronizer {

    void pull(URI uri) throws IOException;
    void push(URI uri) throws IOException;
    void update() throws IOException;
    
}
