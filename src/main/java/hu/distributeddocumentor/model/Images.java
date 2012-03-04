package hu.distributeddocumentor.model;

import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.AddCommand;
import com.aragost.javahg.commands.RemoveCommand;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class Images extends Observable {
    
    private final Repository repository;
    private final File mediaDir;
    private final Set<String> images;

    public Images(Repository repository) {
        this.repository = repository;
        
        File root = repository.getDirectory();
        mediaDir = new File(root, "media");
        
        images = new HashSet<String>();
        
        if (!mediaDir.exists()) {            
            mediaDir.mkdir();
        } else {
            collectImages();
        }                
    }
    
    public void reload() {
        images.clear();
        collectImages();
        
        setChanged();
        notifyObservers();
    }
    
    public void addImage(File externalImage) throws ImageAlreadyExistsException, IOException {
        
        String name = externalImage.getName();
        if (images.contains(name))
            throw new ImageAlreadyExistsException();
        
        File target = new File(mediaDir, name);
        Files.copy(externalImage, target);
        
        AddCommand add = new AddCommand(repository);
        add.execute(target);
        
        images.add(name);
        
        setChanged();
        notifyObservers();
    }
    
    public void removeImage(String name) {
        
        File image = new File(mediaDir, name);
        
        RemoveCommand remove = new RemoveCommand(repository);
        remove.execute(image);
               
        images.remove(name);
        
        setChanged();
        notifyObservers();
    }
    
    public Set<String> getImages() {        
        return images;
    }
    
    private void collectImages() {
        
        for (File img : mediaDir.listFiles()) {
            images.add(img.getName());
        }        
    }

    public File getMediaRoot() {
        return mediaDir;
    }
}
