package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.Snippet;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;


public class SnippetListModel extends AbstractTableModel implements Observer { 

    private final Documentation doc;

    public SnippetListModel(Documentation doc) {
        this.doc = doc;
        doc.addObserver(this);
    }
        
    @Override
    public String getColumnName(int i) {
        
        switch (i) {
            case 0: return "ID";
            default: return null;
        }
    }
    
    
    @Override
    public int getRowCount() {
        return doc.getSnippets().size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
             
        Snippet snippet = doc.getSnippets().toArray(new Snippet[0])[row];
        
        if (col == 0) {
            return snippet.getId();
        } else {            
            return null;
        }
    }

    @Override
    public void update(Observable o, Object o1) {
        fireTableDataChanged();
    }        
}