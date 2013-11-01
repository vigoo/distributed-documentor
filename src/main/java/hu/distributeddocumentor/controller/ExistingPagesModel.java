
package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.Page;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

public class ExistingPagesModel implements ComboBoxModel {

    private final List<Page> pages;
    private String selectedPageId;

    public ExistingPagesModel(Documentation doc) {
        pages = new LinkedList<>(doc.getPages());
    }        
    
    @Override
    public void setSelectedItem(Object anItem) {
        selectedPageId = (String)anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedPageId;
    }

    @Override
    public int getSize() {
        return pages.size();
    }

    @Override
    public Object getElementAt(int index) {
        return pages.get(index).getId();
    }

    @Override
    public void addListDataListener(ListDataListener l) {        
    }

    @Override
    public void removeListDataListener(ListDataListener l) {        
    }
    
}
