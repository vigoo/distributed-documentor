package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.controller.sync.RepoChangeSet;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;


public class ChangesetTableModel implements TableModel {
    
    private final List<RepoChangeSet> changesets;

    public ChangesetTableModel(final List<RepoChangeSet> changesets) {
        this.changesets = changesets;
    }
        

    @Override
    public int getRowCount() {
        return changesets.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int i) {
        switch (i) {
            case 0:
                return "ID";
            case 1:
                return "User";
            case 2:
                return "Date";
            case 3:
                return "Summary";
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        RepoChangeSet cs = changesets.get(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return cs.getRevision();
            case 1:
                return cs.getUser();
            case 2:
                return cs.getTimestamp();
            case 3:
                return cs.getMessage();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object o, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addTableModelListener(TableModelListener tl) {        
    }

    @Override
    public void removeTableModelListener(TableModelListener tl) {        
    }

}
