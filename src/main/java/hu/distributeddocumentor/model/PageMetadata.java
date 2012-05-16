package hu.distributeddocumentor.model;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PageMetadata {
    
    private static final Logger log = LoggerFactory.getLogger(Documentation.class.getName());
    
    private final String pageId;
    private final Properties metadata;
    private boolean hasChanged;

    public PageMetadata(String pageId) {
        
        this.pageId = pageId;
        metadata = new Properties();                        
    }               
    
    public Object get(String key) {
        return metadata.get(key);
    }
    
    public void put(String key, Object value) {
        metadata.put(key, value);        
        hasChanged = true;
    }
    
    public void load(File root) {
        
        File metadataFile = getFile(root);
        if (metadataFile.exists()) {
            
            try {
                InputStream in = new FileInputStream(metadataFile);
                try {
                    metadata.load(in);
                }
                finally {
                    in.close();
                }
            }
            catch (IOException ex) {                
                log.error("Failed to load page metadata", ex);
                metadata.clear();
            }
        }
        
        hasChanged = false;
    }
    
    public void save(File root) {
        File metadataFile = getFile(root);
        
        try {            
            // Writing the file
            OutputStream out = new FileOutputStream(metadataFile);        
            try {
                metadata.store(out, "Metadata of page " + pageId);
            }
            finally {
                out.close();                    
            }
            
            // Removing comments to avoid merging conflicts            
            final List<String> lines = Files.readLines(metadataFile, Charset.defaultCharset());
            lines.remove(0);
            lines.remove(0);
            Files.write(Joiner.on('\n').join(lines), metadataFile, Charset.defaultCharset());
        }
        catch (IOException ex) {                
            log.error("Failed to load page metadata", ex);
            metadata.clear();
        }
        
        hasChanged = false;
    }
        
    public File getFile(File root) {
        return new File(root, pageId+".metadata");        
    }

    public boolean hasChanged() {
        return hasChanged;
    }
}
