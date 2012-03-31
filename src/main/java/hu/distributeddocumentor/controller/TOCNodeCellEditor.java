package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.TOCNode;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;


public class TOCNodeCellEditor extends DefaultTreeCellEditor {

    public TOCNodeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }
    
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        return super.getTreeCellEditorComponent(tree, ((TOCNode)value).getTitle(), isSelected, expanded, leaf, row);
    }  
}
