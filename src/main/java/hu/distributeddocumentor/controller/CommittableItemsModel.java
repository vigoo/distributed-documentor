package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.Change;
import hu.distributeddocumentor.model.Documentation;
import java.util.Arrays;
import java.util.Map;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

public class CommittableItemsModel implements ListModel {

    final String[] ordered;
    final Map<String, Change> changes;
    
    public CommittableItemsModel(Documentation doc) {                        
        
        changes = doc.getVersionControl().getChanges();
        
        Object[] keys = changes.keySet().toArray();
        ordered = Arrays.copyOf(keys, keys.length, String[].class);
    }    
    
    
    @Override
    public int getSize() {
        return changes.size();
    }

    @Override
    public Object getElementAt(int i) {
        
        String path = ordered[i];
        return new CommittableItem(path, changes.get(path));
    }

    @Override
    public void addListDataListener(ListDataListener ll) {                        
    }

    @Override
    public void removeListDataListener(ListDataListener ll) {        
    }
    
}
