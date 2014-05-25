package hu.distributeddocumentor.exporters;

import hu.distributeddocumentor.gui.LongOperationRunner;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.virtual.builders.VirtualNodeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Interface for exporter implementations which can convert and save a documentation
 * to a set of files.
 * @author Daniel Vigovszky
 */
public interface Exporter {
    
    /**
     * Exports a documentation to the format implemented by this exporter
     * @param doc The documentation to be exported
     * @param targetDir Target directory where the exported documentation will be put
     * @param longOp Interface to run long operations
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void export(Documentation doc, File targetDir, LongOperationRunner longOp) throws FileNotFoundException, IOException, VirtualNodeException;

    /**
     * Gets the exporter's target name (such as HTML, PDF, etc.). Used for building UI controls and messages.     
     * @return returns the exporter's target
     */
    public String getTargetName();
}
