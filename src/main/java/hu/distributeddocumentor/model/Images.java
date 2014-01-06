package hu.distributeddocumentor.model;

import com.google.common.io.Files;
import hu.distributeddocumentor.vcs.VersionControl;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 * The observable collection of images belonging to the documentation
 * 
 * <p>
 * The images can be referenced from any page or snippet. They are currently 
 * represented by a flat list, and stored in the media subdirectory
 * of the documentation's root.
 * 
 * @author Daniel Vigovszky
 */
public class Images extends Observable {
    
    private final VersionControl versionControl;
    private final File mediaDir;
    private final Set<String> images;

    /**
     * Initializes the image collection
     * 
     * @param versionControl the version control repository used by the documentation
     * @param relativeRoot the relative path to the documentation's root inside the repository
     */
    public Images(VersionControl versionControl, String relativeRoot) {
        this.versionControl = versionControl;
        
        File root = new File(versionControl.getRoot(), relativeRoot);
        mediaDir = new File(root, "media");
        
        images = new HashSet<>();
        
        if (!mediaDir.exists()) {            
            if (!mediaDir.mkdir()) {
                throw new RuntimeException("Failed to create media directory");
            }
        } else {
            collectImages();
        }                
    }
    
    /**
     * Reloads the images from the repository.
     * 
     * <p>
     * Useful when the repository has been updated to a new revision.
     */
    public void reload() {
        images.clear();
        collectImages();
        
        setChanged();
        notifyObservers();
    }
    
    /**
     * Adds a new image to the collection from an external location
     * 
     * <p>
     * The image will be copied and added to the repository.
     * 
     * @param externalImage path to the image, outside the repository
     * @throws ImageAlreadyExistsException
     * @throws IOException
     */
    public void addImage(File externalImage) throws ImageAlreadyExistsException, IOException {
        
        String name = externalImage.getName();
        if (images.contains(name)) {
            throw new ImageAlreadyExistsException();
        }
        
        File target = new File(mediaDir, name);
        
        if (!externalImage.equals(target)) {
            Files.copy(externalImage, target);
        }
        
        versionControl.add(target);
        
        images.add(name);
        
        setChanged();
        notifyObservers();
    }
    
    /**
     * Removes an image from the collection (and deletes from the repository)
     * 
     * @param name name of the image to be removed
     */
    public void removeImage(String name) {
        
        File image = new File(mediaDir, name);
        
        versionControl.remove(image, false);
               
        images.remove(name);
        
        setChanged();
        notifyObservers();
    }
    
    /**
     * Gets the set of image names stored in the collection
     * 
     * @return returns a set of image file names
     */
    public Set<String> getImages() {        
        return images;
    }
    
    private void collectImages() {
        
        for (File img : mediaDir.listFiles()) {
            images.add(img.getName());
        }        
    }

    /**
     * Gets the media directory here the images are stored
     * 
     * @return the absolute path of the media directory
     */
    public File getMediaRoot() {
        return mediaDir;
    }
}
