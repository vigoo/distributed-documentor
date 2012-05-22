package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.exporters.CHMExporter;
import hu.distributeddocumentor.exporters.Exporter;
import hu.distributeddocumentor.exporters.HTMLExporter;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;


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
            doc.initFromExisting(root);
            
            File target = prefs.getExportTarget();
            System.out.println("Export target directory: " + target.getAbsolutePath());
            
            Exporter exporter = null;
            if (prefs.exportToHTML()) {
                
                System.out.println("Exporting to static HTML pages...");
                exporter = new HTMLExporter(doc, target);
            }
            else if (prefs.exportToCHM()) {
                
                System.out.println("Exporting to CHM...");
                exporter = new CHMExporter(prefs, doc, target);
            }
            
            exporter.export();
            
            System.out.println("Export finished");
        }
        catch (Exception ex) {
            System.err.println("Export failed. Reason: " + ex.getMessage());
        }
    }
}
