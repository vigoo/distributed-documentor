package hu.distributeddocumentor.gui;

import com.google.inject.Inject;
import com.jidesoft.swing.FolderChooser;
import hu.distributeddocumentor.exporters.Exporter;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates export menu items dynamically, and performs the export
* actions when the menu item is clicked.
 * @author Daniel Vigovszky
 */
public class ExportMenu {
    private static final Logger log = LoggerFactory.getLogger(ExportMenu.class.getName());
    private final Set<Exporter> exporters;
    private final DocumentorPreferences prefs;
    
    @Inject
    public ExportMenu(Set<Exporter> exporters, DocumentorPreferences prefs) {        
        this.exporters = exporters;
        this.prefs = prefs;
    }
    
    public void buildMenu(final Frame parent, final JMenu exportMenuItem, final Documentation doc) {
        
        for (final Exporter exporter : exporters) {
            
            JMenuItem item = new JMenuItem("Export to " + exporter.getTargetName() + "...");
            
            if (prefs.getDefaultExporter().getTargetName().equals(exporter.getTargetName())) {
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            }
            
            item.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            exportDocumentation(parent, doc, exporter);
                        }
            });
            exportMenuItem.add(item);
        }
    }
    
    private void exportDocumentation(Frame parent, Documentation doc, Exporter exporter) {
                 
        java.util.List<String> recent = prefs.getRecentTargets();
        
        FolderChooser chooser;
        
        if (recent.size() > 0) {
            chooser = new FolderChooser(recent.get(0));
        }
        else {
            chooser = new FolderChooser();
        }        
        chooser.setDialogTitle("Select the target folder");

        chooser.setRecentList(recent);
        chooser.setRecentListVisible(true);
                
        if (chooser.showOpenDialog(parent) == FolderChooser.APPROVE_OPTION) {
            
            File targetDir = chooser.getSelectedFile();
            String path = targetDir.getAbsolutePath();
         
            try {
                doc.suspendProcessingOrphanedPages();
                exporter.export(doc, targetDir, LongOperation.get());
            }
            catch (Exception ex) {
                log.error(null, ex);
                
                ErrorDialog.show(parent, "Export failed", ex);
            }
            finally {
                doc.resumeProcessingOrphanedPages();
            }
            
            recent.remove(path);     
            recent.add(0, path);
            prefs.setRecentTargets(recent);                        
        }
    }

}
