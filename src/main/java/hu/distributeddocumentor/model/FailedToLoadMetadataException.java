package hu.distributeddocumentor.model;


public class FailedToLoadMetadataException extends Exception {

    public FailedToLoadMetadataException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "Failed to load documentation metadata";
    }
   
    
}
