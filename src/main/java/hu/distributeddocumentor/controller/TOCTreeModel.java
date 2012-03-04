package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.TOC;
import hu.distributeddocumentor.model.TOCNode;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author vigoo
 */
public class TOCTreeModel implements TreeModel {

    private final TOC toc;

    public TOCTreeModel(TOC toc) {
        this.toc = toc;        
    }
    
    @Override
    public Object getRoot() {
        return toc.getRoot();
    }

    @Override
    public Object getChild(Object o, int i) {        
        TOCNode node = (TOCNode)o;
        return node.getChildren().get(i);
    }

    @Override
    public int getChildCount(Object o) {
        TOCNode node = (TOCNode)o;
        return node.getChildren().size();
    }

    @Override
    public boolean isLeaf(Object o) {
        return getChildCount(o) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath tp, Object o) {
        
        TOCNode node = (TOCNode)tp.getLastPathComponent();
       
        toc.changeNodeTitle(node, (String)o);
    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {    
        TOCNode node = (TOCNode)o;
        TOCNode child = (TOCNode)o1;
        
        return node.getChildren().indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener tl) {
        toc.addTreeModelListener(tl);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener tl) {
        toc.removeTreeModelListener(tl);
    }    
}
