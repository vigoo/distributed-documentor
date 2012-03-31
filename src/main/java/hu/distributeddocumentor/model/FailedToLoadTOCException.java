package hu.distributeddocumentor.model;


class FailedToLoadTOCException extends Exception {

    public FailedToLoadTOCException(Exception ex) {
        
        super("Failed to load TOC", ex);
    }
    
}
