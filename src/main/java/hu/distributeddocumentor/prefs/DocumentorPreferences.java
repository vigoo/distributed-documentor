package hu.distributeddocumentor.prefs;

import java.io.File;
import java.util.prefs.Preferences;

public class DocumentorPreferences {

    private final Preferences prefs;
    
    public DocumentorPreferences() {
        
        prefs = Preferences.userRoot().node(this.getClass().getName());        
    }
    
    public String getMercurialPath() {        
        return prefs.get("hgpath", null);
    }
    
    public void setMercurialPath(String path) {
        prefs.put("hgpath", path);
    }
    
    public String getCHMCompilerPath() {
        return prefs.get("hhcpath", null);
    }
    
    public void setCHMCompilerPath(String path) {
        prefs.put("hhcpath", path);
    }
 
    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public boolean hasValidMercurialPath() {
        String path = getMercurialPath();
        
        if (path != null) {
            
            File f = new File(path);
            if (f.exists() && f.canExecute())
                return true;
        }
        
        return false;
    }

    public boolean hasValidCHMCompilerPath() {
        String path = getCHMCompilerPath();
        
        if (path != null) {
            
            File f = new File(path);
            if (f.exists() && f.canExecute())
                return true;
        }
        
        return false;
    }
}
