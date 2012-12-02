
package hu.distributeddocumentor.gui;

import hu.distributeddocumentor.model.Page;

/**
 * Interface for the floating preview window
 * @author Daniel Vigovszky
 */
public interface FloatingPreview {
    
    /** 
     * Enables the floating preview
     */
    public void show();
    
    /**
     * Disables the floating preview
     */
    public void hide();
    
    /**
     * Sets the page to be rendered in the preview
     * @param newPage the page to be rendered in the floating preview
     */
    public void switchPage(Page newPage);
    
    /**
     * Gets the preview scroll synchronization interface
     * @return the PreviewSync implementation of the floating preview
     */
    public PreviewSync getSync();
}
