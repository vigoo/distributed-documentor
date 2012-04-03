package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.Snippet;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractListModel;


public class SnippetListModel extends AbstractListModel implements Observer { 

    private final Documentation doc;

    public SnippetListModel(Documentation doc) {
        this.doc = doc;
        doc.addObserver(this);
    }
    
    @Override
    public int getSize() {
        return doc.getSnippets().size();
    }

    @Override
    public Object getElementAt(int i) {
        Object[] arr = doc.getSnippets().toArray();
        Snippet snippet = (Snippet)arr[i];
        return snippet.getId();
    }

    @Override
    public void update(Observable o, Object o1) {
        
        // TODO: fire the correct event
        fireContentsChanged(this, 0, getSize());
    }
    
    
}