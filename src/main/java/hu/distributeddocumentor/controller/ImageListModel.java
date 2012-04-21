package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.Images;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author vigoo
 */
public class ImageListModel extends AbstractTableModel implements Observer {
    
    private final Images images;

    public ImageListModel(Images images) {
        this.images = images;
        
        images.addObserver(this);
    }

    @Override
    public String getColumnName(int i) {
        
        switch (i) {
            case 0: return "Name";
            default: return null;
        }
    }
    
    
    @Override
    public int getRowCount() {
        return images.getImages().size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        
        String name = images.getImages().toArray(new String[0])[row];
        
        if (col == 0) {
            return name;
        } else {            
            return null;
        }
    }

    @Override
    public void update(Observable o, Object o1) {
        fireTableDataChanged();
    }
    
}
