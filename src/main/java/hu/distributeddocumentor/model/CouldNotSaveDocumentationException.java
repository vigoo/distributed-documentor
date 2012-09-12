package hu.distributeddocumentor.model;

/**
 * Exception thrown if the documentation could not be saved
 * 
 * @author Daniel Vigovszky
 */
public class CouldNotSaveDocumentationException extends Exception {

    /**
     * Creates the exception object
     * 
     * @param inner The cause of the problem
     */
    public CouldNotSaveDocumentationException(Exception inner) {
    }
    
}
