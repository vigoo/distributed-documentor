package hu.distributeddocumentor.exporters;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Exporter {
    
    void export() throws FileNotFoundException, IOException ;
}
