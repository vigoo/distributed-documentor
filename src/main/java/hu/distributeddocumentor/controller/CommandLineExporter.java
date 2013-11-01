package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.exporters.Exporter;
import hu.distributeddocumentor.exporters.chm.CHMExporter;
import hu.distributeddocumentor.exporters.html.HTMLExporter;
import hu.distributeddocumentor.gui.LongOperationRunner;
import hu.distributeddocumentor.gui.SimpleLongOperationRunner;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.FailedToLoadMetadataException;
import hu.distributeddocumentor.model.FailedToLoadPageException;
import hu.distributeddocumentor.model.FailedToLoadTOCException;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.io.IOException;


public class CommandLineExporter {
    
    private final DocumentorPreferences prefs;

    public CommandLineExporter(DocumentorPreferences prefs) {
        this.prefs = prefs;
    }
    
    public void run() {
        
        File root = prefs.getInitialRoot();
        
        System.out.println("Loading documentation from " + root.getAbsolutePath());
        
        Documentation doc = new Documentation(prefs);
        try {
            LongOperationRunner longOp = new SimpleLongOperationRunner();
            
            doc.initFromExisting(root, longOp);
            
            File target = prefs.getExportTarget();
            System.out.println("Export target directory: " + target.getAbsolutePath());
            
            Exporter exporter = null;
            if (prefs.exportToHTML()) {
                
                System.out.println("Exporting to static HTML pages...");
                exporter = prefs.getInjector().getInstance(HTMLExporter.class);
            }
            else if (prefs.exportToCHM()) {
                
                System.out.println("Exporting to CHM...");
                exporter = prefs.getInjector().getInstance(CHMExporter.class);
            }
            
            exporter.export(doc, target, longOp);
            
            System.out.println("Export finished");
        }
        catch (FailedToLoadPageException | FailedToLoadTOCException | FailedToLoadMetadataException | IOException ex) {
            System.err.println("Export failed. Reason: " + ex.getMessage());
        }
    }
}
