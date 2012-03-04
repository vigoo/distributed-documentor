package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.Images;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractListModel;

/**
 *
 * @author vigoo
 */
public class ImageListModel extends AbstractListModel implements Observer {
    
    private final Images images;

    public ImageListModel(Images images) {
        this.images = images;
        
        images.addObserver(this);
    }

    @Override
    public int getSize() {
        return images.getImages().size();
    }

    @Override
    public Object getElementAt(int i) {
        Object[] arr = images.getImages().toArray();
        return arr[i];
    }

    @Override
    public void update(Observable o, Object o1) {
        
        // TODO: fire the correct event
        fireContentsChanged(this, 0, getSize());
    }
    
}
