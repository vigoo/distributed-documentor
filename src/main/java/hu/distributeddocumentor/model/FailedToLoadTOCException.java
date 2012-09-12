package hu.distributeddocumentor.model;


/**
 * Exception thrown when the TOC could not be loaded
 * 
 * @author Daniel Vigovszky
 * @see TOC
 */
public class FailedToLoadTOCException extends Exception {

    /**
     * Constructs the exception object
     * 
     * @param ex reason why the TOC could not be loaded
     */
    public FailedToLoadTOCException(Exception ex) {
        
        super("Failed to load TOC", ex);
    }
    
}
