package hu.distributeddocumentor.prefs;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentorPreferences {

    private static final Logger logger = LoggerFactory.getLogger(DocumentorPreferences.class.getName());
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
    
    public List<String> getRecentRepositories() {        
        
        try {
            List<String> result = new LinkedList<String>();
            Preferences reposNode = prefs.node("recentRepositories");

            for (String key : reposNode.keys()) {
                String item = reposNode.get(key, null);
                if (item != null)
                    result.add(item);
            }

            return result;
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
            return new LinkedList<String>();
        }
    }
    
    public void setRecentRepositories(List<String> list) {
        
        try {
            Preferences reposNode = prefs.node("recentRepositories");

            for (String key : reposNode.keys())
                reposNode.remove(key);
            
            for (int i = 0; i < list.size(); i++) {
                reposNode.put(Integer.toString(i), list.get(i));
            }
            
            prefs.flush();
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
        }
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
