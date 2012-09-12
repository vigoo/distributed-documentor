package hu.distributeddocumentor.model;

import java.io.File;

/**
 * Exception thrown when a page could not be loaded
 * 
 * @author Daniel Vigovszky
 * @see Page
 */
public class FailedToLoadPageException extends Exception {
    
    private final File pageFile;

    /**
     * Constructs the exception object
     * @param child the page's file representation which could not be loaded
     * @param ex the cause of the problem
     */
    public FailedToLoadPageException(File child, Exception ex) {
        super(ex);
        
        this.pageFile = child;
    }

    @Override
    public String getMessage() {
        
        return "Failed to load page from " + pageFile.getAbsolutePath();        
    }       
}
