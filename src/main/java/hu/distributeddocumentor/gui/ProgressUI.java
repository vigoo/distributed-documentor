
package hu.distributeddocumentor.gui;


/**
 * Generic progress report interface 
 */
public interface ProgressUI {

    /**
     * Sets the currents status of the progress
     * @param status the status message
     */
    public void setStatus(String status);
    
    /**
     * Sets the current percentage of completeness
     * @param percentage percentage (value between 0 and 1)
     */
    public void setProgress(double percentage);
    
    /**
     * Marks the progress as indeterminate
     */
    public void setIndeterminate();
 
}
