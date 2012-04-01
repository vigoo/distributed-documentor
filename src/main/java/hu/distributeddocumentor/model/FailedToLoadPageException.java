package hu.distributeddocumentor.model;

import java.io.File;

public class FailedToLoadPageException extends Exception {
    
    private final File pageFile;

    public FailedToLoadPageException(File child, Exception ex) {
        super(ex);
        
        this.pageFile = child;
    }

    @Override
    public String getMessage() {
        
        return "Failed to load page from " + pageFile.getAbsolutePath();        
    }       
}
