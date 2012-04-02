package hu.distributeddocumentor.gui;

import hu.distributeddocumentor.controller.PageIdGenerator;
import hu.distributeddocumentor.controller.TOCNodeCellEditor;
import hu.distributeddocumentor.controller.TOCTreeModel;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOC;
import hu.distributeddocumentor.model.TOCNode;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author vigoo
 */
public class TableOfContentsView extends javax.swing.JPanel {

    private final Documentation doc;
    private final TOC toc;
    private final PageEditorHost pageEditorHost;
    
    /**
     * Creates new form TableOfContentsView
     */
    public TableOfContentsView(Documentation doc, PageEditorHost pageEditorHost) {
        this.doc = doc;
        toc = doc.getTOC();
        this.pageEditorHost = pageEditorHost;
        
        initComponents();      
        
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellEditor(new TOCNodeCellEditor(tree, (DefaultTreeCellRenderer)tree.getCellRenderer()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        jToolBar1 = new javax.swing.JToolBar();
        btAdd = new javax.swing.JButton();
        btRemove = new javax.swing.JButton();
        btUp = new javax.swing.JButton();
        btDown = new javax.swing.JButton();
        btLeft = new javax.swing.JButton();
        btRight = new javax.swing.JButton();

        tree.setModel(new TOCTreeModel(toc)       );
        tree.setEditable(true);
        tree.setShowsRootHandles(true);
        tree.setToggleClickCount(0);
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tree);

        jToolBar1.setRollover(true);

        btAdd.setText("Add");
        btAdd.setFocusable(false);
        btAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddActionPerformed(evt);
            }
        });
        jToolBar1.add(btAdd);

        btRemove.setText("Remove");
        btRemove.setFocusable(false);
        btRemove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveActionPerformed(evt);
            }
        });
        jToolBar1.add(btRemove);

        btUp.setText("Up");
        btUp.setFocusable(false);
        btUp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btUp.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btUpActionPerformed(evt);
            }
        });
        jToolBar1.add(btUp);

        btDown.setText("Down");
        btDown.setFocusable(false);
        btDown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btDown.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDownActionPerformed(evt);
            }
        });
        jToolBar1.add(btDown);

        btLeft.setText("<");
        btLeft.setFocusable(false);
        btLeft.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btLeft.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLeftActionPerformed(evt);
            }
        });
        jToolBar1.add(btLeft);

        btRight.setText(">");
        btRight.setFocusable(false);
        btRight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btRight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRightActionPerformed(evt);
            }
        });
        jToolBar1.add(btRight);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1)
                .addContainerGap())
            .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void treeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMousePressed
        
        if (evt.getClickCount() == 2) {
            // On double click...
            
            TreePath path = tree.getPathForLocation(evt.getX(), evt.getY());
            TOCNode node = (TOCNode)path.getLastPathComponent();
            
            if (node.hasTarget()) {
                String pageId = node.getTarget().getId();
                pageEditorHost.openOrFocusPage(pageId);
            } else {
                               
                if (node != toc.getUnorganized() &&
                    node != toc.getRoot() &&
                    node != toc.getRecycleBin()) {
                    
                    PageIdGenerator idGenerator = new PageIdGenerator(doc);
                    String id = idGenerator.generate(node.getTitle());
                    
                    CreateNewPageDialog dlg = new CreateNewPageDialog(pageEditorHost.getMainFrame(), true, doc, id);
                    dlg.setVisible(true);

                    if (dlg.getReturnStatus() == CreateNewPageDialog.RET_OK) {
                        String newID = dlg.getID();
                        String newLang = dlg.getMarkupLanguage();

                        Page page = new Page(newID, doc);
                        page.setMarkupLanguage(newLang);                    
                        node.setTarget(page);

                        try {                            
                            doc.addNewPage(page);
                        } catch (Exception ex) {                        
                            Logger.getLogger(TableOfContentsView.class.getName()).log(Level.SEVERE, null, ex);

                            JOptionPane.showMessageDialog(this, ex.getMessage(), "Failed to add new page", JOptionPane.ERROR_MESSAGE);
                        }

                        pageEditorHost.openOrFocusPage(newID);
                    }
                }
            }
        }
        
    }//GEN-LAST:event_treeMousePressed

    private void btAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddActionPerformed
        
        Object selection = tree.getSelectionPath() == null ? null : tree.getSelectionPath().getLastPathComponent();
        TOCNode node = selection == null ? toc.getRoot() : (TOCNode)selection;
        toc.addToEnd(node, new TOCNode("New node"));
        
    }//GEN-LAST:event_btAddActionPerformed

    private void btRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveActionPerformed
        Object selection = tree.getSelectionPath() == null ? null : tree.getSelectionPath().getLastPathComponent();
        TOCNode node = selection == null ? null : (TOCNode)selection;
        
        if (node != null) {
            if (node != toc.getRoot() &&
                node != toc.getUnorganized() &&
                node != toc.getRecycleBin())
                toc.remove(node);
        }
    }//GEN-LAST:event_btRemoveActionPerformed

    private void btUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btUpActionPerformed
        
        Object selection = tree.getSelectionPath() == null ? null : tree.getSelectionPath().getLastPathComponent();
        TOCNode node = selection == null ? null : (TOCNode)selection;
        
        if (node != null) {
            toc.moveUp(node);
            
            tree.setSelectionPath(new TreePath(node.toPath()));
        }
        
    }//GEN-LAST:event_btUpActionPerformed

    private void btDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDownActionPerformed
        
        Object selection = tree.getSelectionPath() == null ? null : tree.getSelectionPath().getLastPathComponent();
        TOCNode node = selection == null ? null : (TOCNode)selection;
        
        if (node != null) {
            toc.moveDown(node);
            
            tree.setSelectionPath(new TreePath(node.toPath()));
        }
        
    }//GEN-LAST:event_btDownActionPerformed

    private void btLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLeftActionPerformed
        
        Object selection = tree.getSelectionPath() == null ? null : tree.getSelectionPath().getLastPathComponent();
        TOCNode node = selection == null ? null : (TOCNode)selection;
        
        if (node != null) {
            toc.moveLeft(node);
            
            tree.setSelectionPath(new TreePath(node.toPath()));
        }
    }//GEN-LAST:event_btLeftActionPerformed

    private void btRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRightActionPerformed
    
        Object selection = tree.getSelectionPath() == null ? null : tree.getSelectionPath().getLastPathComponent();
        TOCNode node = selection == null ? null : (TOCNode)selection;
        
        if (node != null) {                    
            toc.moveRight(node);
            
            tree.setSelectionPath(new TreePath(node.toPath()));
        }
    }//GEN-LAST:event_btRightActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btDown;
    private javax.swing.JButton btLeft;
    private javax.swing.JButton btRemove;
    private javax.swing.JButton btRight;
    private javax.swing.JButton btUp;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables

}
