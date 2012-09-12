package hu.distributeddocumentor.model;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Stores additional, extensible information about a page, which is not supported
 * by the page's markup.
 * 
 * <p>
 * The metadata values are serialized using the {@link Properties} class.
 * 
 * @author Daniel Vigovszky
 */
public class PageMetadata {
    
    private static final Logger log = LoggerFactory.getLogger(Documentation.class.getName());
    
    private final String pageId;
    private final Properties metadata;
    private boolean hasChanged;

    /**
     * Initializes the metadata store for a given page
     * 
     * @param pageId identifier of the page
     */
    public PageMetadata(String pageId) {
        
        this.pageId = pageId;
        metadata = new Properties();                        
    }               
    
    /**
     * Gets a stored value by its key
     * 
     * @param key key to the requested metadata value
     * @return returns the stored value or null if it does not exist
     */
    public Object get(String key) {
        return metadata.get(key);
    }
    
    /**
     * Puts a value to the metadata store
     * 
     * @param key key of the metadata value
     * @param value the value itself
     */
    public void put(String key, Object value) {
        metadata.put(key, value);        
        hasChanged = true;
    }
    
    /**
     * Loads the stored metadata from a file
     * 
     * @param root directory where the page files and metadata are stored
     */
    public void load(File root) {
        
        File metadataFile = getFile(root);
        if (metadataFile.exists()) {
          
           try (InputStream in = new FileInputStream(metadataFile)) {
                metadata.load(in);
           }
           catch (IOException ex) {                
                log.error("Failed to load page metadata", ex);
                metadata.clear();
           }
        }
        
        hasChanged = false;
    }
    
    /**
     * Saves the metadata to the file system
     * 
     * @param root the directory where the file should be placed
     */
    public void save(File root) {
        File metadataFile = getFile(root);
        
        try {            
            try (OutputStream out = new FileOutputStream(metadataFile)) {
                metadata.store(out, "Metadata of page " + pageId);
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
        
    /**
     * Gets the file name of the metadata store
     * 
     * @param root the directory where the page files are stored
     * @return the absolute path of the metadata file
     */
    public File getFile(File root) {
        return new File(root, pageId+".metadata");        
    }

    /**
     * Checks if the metadata has been changed since it was saved or loaded
     * 
     * @return true if the metadata has been changed
     */
    public boolean hasChanged() {
        return hasChanged;
    }
}
