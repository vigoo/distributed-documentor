package hu.distributeddocumentor.model;


public class FailedToLoadTOCException extends Exception {

    public FailedToLoadTOCException(Exception ex) {
        
        super("Failed to load TOC", ex);
    }
    
}
