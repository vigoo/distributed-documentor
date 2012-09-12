package hu.distributeddocumentor.model;

/**
 * Exception thrown if a page is already added to the documentation with the
 * same unique identifier.
 * 
 * @author Daniel Vigovszky
 */
public class PageAlreadyExistsException extends Exception {

    /**
     * Constructs the exception object
     */
    public PageAlreadyExistsException() {
    }   
}
