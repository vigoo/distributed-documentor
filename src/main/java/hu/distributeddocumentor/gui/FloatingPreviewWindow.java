package hu.distributeddocumentor.gui;

import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import javax.swing.JFrame;


public class FloatingPreviewWindow extends JFrame implements FloatingPreview {

    private final HTMLPreview preview;
    
    public FloatingPreviewWindow(final File root, final PageEditorHost host, final DocumentorPreferences prefs) {
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        preview = new HTMLPreview(null, host, root);
        add(preview);
        pack();
        
        setLocation(prefs.getFloatingPreviewX(), prefs.getFloatingPreviewY());
        setSize(prefs.getFloatingPreviewWidth(), prefs.getFloatingPreviewHeight());
        
        addComponentListener(
                new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                prefs.setFloatingPreviewWidth(getWidth());
                prefs.setFloatingPreviewHeight(getHeight());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                prefs.setFloatingPreviewX(getX());
                prefs.setFloatingPreviewY(getY());
            }

            @Override
            public void componentShown(ComponentEvent e) {                
            }

            @Override
            public void componentHidden(ComponentEvent e) {                
            }
        });
    }
        
    
    @Override
    public void switchPage(Page newPage) {
        preview.switchPage(newPage);
        
        if (newPage != null) {
            setTitle("Page preview: " + newPage.getId());
        } else {
            setTitle("Page preview");
        }
    }

    @Override
    public PreviewSync getSync() {
        return preview;
    }

}
