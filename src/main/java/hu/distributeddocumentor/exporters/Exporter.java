package hu.distributeddocumentor.exporters;

import hu.distributeddocumentor.model.Documentation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface Exporter {
    
    void export(Documentation doc, File targetDir) throws FileNotFoundException, IOException ;
}
