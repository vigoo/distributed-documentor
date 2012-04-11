package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.TOCNode;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

public class UnorganizedPagesModel implements ComboBoxModel {

    private final TOCNode unorganizedPages;
    private TOCNode selectedNode;

    public TOCNode getSelectedNode() {
        return selectedNode;
    }        
    
    public UnorganizedPagesModel(TOCNode unorganizedPages) {
        this.unorganizedPages = unorganizedPages;
        
        if (unorganizedPages.getChildren().size() > 0)
            selectedNode = unorganizedPages.getChildren().get(0);                
        else
            selectedNode = null;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedNode = (TOCNode)anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedNode;
    }

    @Override
    public int getSize() {
        return unorganizedPages.getChildren().size();
    }

    @Override
    public Object getElementAt(int index) {
        return unorganizedPages.getChildren().get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {        
    }

    @Override
    public void removeListDataListener(ListDataListener l) {        
    }
    
}
