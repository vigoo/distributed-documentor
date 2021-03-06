package hu.distributeddocumentor.gui;

import com.jidesoft.swing.FolderChooser;
import com.jidesoft.swing.SearchableUtils;
import hu.distributeddocumentor.controller.PageIdGenerator;
import hu.distributeddocumentor.controller.TOCNodeCellEditor;
import hu.distributeddocumentor.controller.TOCTreeModel;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.PageAlreadyExistsException;
import hu.distributeddocumentor.model.toc.DefaultVirtualTOCNode;
import hu.distributeddocumentor.model.toc.TOC;
import hu.distributeddocumentor.model.toc.TOCNode;
import hu.distributeddocumentor.model.virtual.builders.docxml.DocXmlHierarchyBuilder;
import hu.distributeddocumentor.model.virtual.builders.merge.DocumentationMerger;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.utils.ResourceUtils;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.slf4j.LoggerFactory;

public class TableOfContentsView extends javax.swing.JPanel {

    private final Documentation doc;
    private final TOC toc;
    private final PageEditorHost pageEditorHost;
    private final DocumentorPreferences prefs;
    
    private final ImageIcon virtualNodeIcon;
    
    private TOCNode contextMenuTarget;        
    
    /**
     * Creates new form TableOfContentsView
     */
    public TableOfContentsView(final Documentation doc, PageEditorHost pageEditorHost, DocumentorPreferences prefs) {
        this.doc = doc;
        toc = doc.getTOC();
        this.pageEditorHost = pageEditorHost;
        this.prefs = prefs;
        
        initComponents();   
        
        virtualNodeIcon = new ImageIcon(TableOfContentsView.class.getResource("gears.png"));
        
        SearchableUtils.installSearchable(tree);
        
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {      
            
                TOCNode node = (TOCNode)value;
                if (node.hasTarget()) {
                    setBackgroundNonSelectionColor(
                            doc.getStatusColor(
                                (String)node.getTarget().getMetadata().get("Status")));
                } else {
                    setBackgroundNonSelectionColor(
                            getBackground());
                }
                                                                
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                
                if (node instanceof DefaultVirtualTOCNode) {
                    setIcon(virtualNodeIcon);
                }
                
                return this;
            }  
        });
        
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellEditor(new TOCNodeCellEditor(tree, (DefaultTreeCellRenderer)tree.getCellRenderer()));                
    }

    private void createNewPage(TOCNode node) throws HeadlessException {
        PageIdGenerator idGenerator = new PageIdGenerator(doc);
        String id = idGenerator.generate(node.getTitle());
        
        CreateNewPageDialog dlg = new CreateNewPageDialog(pageEditorHost.getMainFrame(), true, doc, id);
        dlg.setVisible(true);

        if (dlg.getReturnStatus() == CreateNewPageDialog.RET_OK) {
            String newID = dlg.getID();
            String newLang = dlg.getMarkupLanguage();

            Page page = new Page(newID, doc, prefs.getConditions(), doc.getCustomStylesheet());
            page.setMarkupLanguage(newLang);                    
            
            toc.changeNodeTarget(node, page);

            try {                            
                doc.addNewPage(page);
            } catch (PageAlreadyExistsException | IOException ex) {                        
                LoggerFactory.getLogger(TableOfContentsView.class.getName()).error(null, ex);

                JOptionPane.showMessageDialog(this, ex.getMessage(), "Failed to add new page", JOptionPane.ERROR_MESSAGE);
            }

            pageEditorHost.openOrFocusPage(newID, "");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenu = new javax.swing.JPopupMenu();
        miOpen = new javax.swing.JMenuItem();
        miProperties = new javax.swing.JMenuItem();
        miCreateNewPage = new javax.swing.JMenuItem();
        miAssignUnorganizedPage = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenu1 = new javax.swing.JMenu();
        miSetAsDocXmlRoot = new javax.swing.JMenuItem();
        miSetAsMergeRoot = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        miRemove = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        jToolBar1 = new javax.swing.JToolBar();
        btAdd = new javax.swing.JButton();
        btRemove = new javax.swing.JButton();
        btUp = new javax.swing.JButton();
        btDown = new javax.swing.JButton();
        btLeft = new javax.swing.JButton();
        btRight = new javax.swing.JButton();

        miOpen.setFont(miOpen.getFont().deriveFont(miOpen.getFont().getStyle() | java.awt.Font.BOLD));
        miOpen.setText("Open");
        miOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miOpenActionPerformed(evt);
            }
        });
        popupMenu.add(miOpen);

        miProperties.setText("Properties");
        miProperties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miPropertiesActionPerformed(evt);
            }
        });
        popupMenu.add(miProperties);

        miCreateNewPage.setText("Create new page...");
        miCreateNewPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCreateNewPageActionPerformed(evt);
            }
        });
        popupMenu.add(miCreateNewPage);

        miAssignUnorganizedPage.setText("Assign unorganized...");
        miAssignUnorganizedPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAssignUnorganizedPageActionPerformed(evt);
            }
        });
        popupMenu.add(miAssignUnorganizedPage);
        popupMenu.add(jSeparator2);

        jMenu1.setText("Special");

        miSetAsDocXmlRoot.setText("DocXml root node");
        miSetAsDocXmlRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSetAsDocXmlRootActionPerformed(evt);
            }
        });
        jMenu1.add(miSetAsDocXmlRoot);

        miSetAsMergeRoot.setText("Merged documentation root node");
        miSetAsMergeRoot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSetAsMergeRootActionPerformed(evt);
            }
        });
        jMenu1.add(miSetAsMergeRoot);

        popupMenu.add(jMenu1);
        popupMenu.add(jSeparator1);

        miRemove.setText("Remove");
        miRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miRemoveActionPerformed(evt);
            }
        });
        popupMenu.add(miRemove);

        tree.setModel(new TOCTreeModel(toc)       );
        tree.setEditable(true);
        tree.setShowsRootHandles(true);
        tree.setToggleClickCount(0);
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                treeMouseReleased(evt);
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
        
        if (evt.isPopupTrigger()) {                     
            onPopup(evt);            
        } 
        else if (evt.getClickCount() == 2) {
            // On double click...
            
            TreePath path = tree.getPathForLocation(evt.getX(), evt.getY());
            
            if (path != null) {
                TOCNode node = (TOCNode)path.getLastPathComponent();

                if (node.hasTarget()) {
                    String pageId = node.getTarget().getId();
                    pageEditorHost.openOrFocusPage(pageId, "");
                } else {

                    if (node != toc.getUnorganized() &&
                        node != toc.getRoot() &&
                        node != toc.getRecycleBin()) {
                        createNewPage(node);
                    }
                }
            }
        }
        
    }//GEN-LAST:event_treeMousePressed

    private void btAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddActionPerformed
        
        Object selection = tree.getSelectionPath() == null ? null : tree.getSelectionPath().getLastPathComponent();
        TOCNode node = selection == null ? toc.getRoot() : (TOCNode)selection;
        toc.addToEnd(node, toc.getFactory().createNode("New node"));
        
    }//GEN-LAST:event_btAddActionPerformed

    private void btRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveActionPerformed
        Object selection = tree.getSelectionPath() == null ? null : tree.getSelectionPath().getLastPathComponent();
        TOCNode node = selection == null ? null : (TOCNode)selection;
        
        if (node != null) {
            if (node != toc.getRoot() &&
                node != toc.getUnorganized() &&
                node != toc.getRecycleBin()) {
                toc.remove(node);
            }
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

    private void treeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseReleased
        
        if (evt.isPopupTrigger()) {                     
            onPopup(evt);            
        }
    }//GEN-LAST:event_treeMouseReleased

    private void miOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miOpenActionPerformed
        
        String pageId = contextMenuTarget.getTarget().getId();
        pageEditorHost.openOrFocusPage(pageId, "");
    }//GEN-LAST:event_miOpenActionPerformed

    private void miCreateNewPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miCreateNewPageActionPerformed
        
        createNewPage(contextMenuTarget);
    }//GEN-LAST:event_miCreateNewPageActionPerformed

    private void miAssignUnorganizedPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAssignUnorganizedPageActionPerformed
        
        SelectExistingUnorganizedPageDialog dlg = new SelectExistingUnorganizedPageDialog(pageEditorHost.getMainFrame(), toc.getUnorganized());
        dlg.setVisible(true);
        if (dlg.getReturnStatus() == SelectExistingUnorganizedPageDialog.RET_OK) {
            TOCNode selected = dlg.getSelectedNode();
            if (selected != null) {
                
                Page page = selected.getTarget();
                toc.changeNodeTarget(contextMenuTarget, page);
                pageEditorHost.openOrFocusPage(page.getId(), "");
            }
        }
    }//GEN-LAST:event_miAssignUnorganizedPageActionPerformed

    private void miRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miRemoveActionPerformed
        toc.remove(contextMenuTarget);
    }//GEN-LAST:event_miRemoveActionPerformed

    private void miSetAsDocXmlRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSetAsDocXmlRootActionPerformed
              
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setDialogTitle("Choose DocXml file...");
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        
        FileFilter xmlFilter = new FileNameExtensionFilter("DocXml files", "xml");
        
        chooser.addChoosableFileFilter(xmlFilter);
        chooser.setFileFilter(xmlFilter);
        
        if (chooser.showDialog(pageEditorHost.getMainFrame(), null) == JFileChooser.APPROVE_OPTION) {
            
            File xmlFile = chooser.getSelectedFile(); 
            
            try {
                String relativePath = ResourceUtils.getRelativePath(xmlFile.getAbsolutePath(), doc.getRepositoryRoot());                                    
                toc.convertToVirtualRoot(contextMenuTarget, DocXmlHierarchyBuilder.class, relativePath);
            }
            catch (Exception ex) {
                ErrorDialog.show(pageEditorHost.getMainFrame(), "Failed to create DocXml root node", ex);
            }
        }
    }//GEN-LAST:event_miSetAsDocXmlRootActionPerformed

    private void miPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miPropertiesActionPerformed
        
        if (contextMenuTarget.hasTarget()) {
            
            PagePropertiesDialog dlg = new PagePropertiesDialog(pageEditorHost.getMainFrame(), true, doc, contextMenuTarget.getTarget(), pageEditorHost);
            dlg.setVisible(true);            
        }
        
    }//GEN-LAST:event_miPropertiesActionPerformed

    private void miSetAsMergeRootActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSetAsMergeRootActionPerformed
        
        List<String> recent = prefs.getRecentRepositories();
        
        FolderChooser chooser;
        if (recent.size() > 0) {
            chooser = new FolderChooser(recent.get(0));
        }
        else {
            chooser = new FolderChooser();
        }
        
        chooser.setDialogTitle("Documentation to be merged");
        chooser.setRecentList(recent);
        chooser.setRecentListVisible(true);        
                
        if (chooser.showOpenDialog(pageEditorHost.getMainFrame()) == FolderChooser.APPROVE_OPTION) {
            File repositoryRoot = chooser.getSelectedFolder();
            
            try {
                String relativePath = ResourceUtils.getRelativePath(repositoryRoot.getAbsolutePath(), doc.getRepositoryRoot());                                                                
                toc.convertToVirtualRoot(contextMenuTarget, DocumentationMerger.class, relativePath);
            }
            catch (Exception ex) {
                ErrorDialog.show(pageEditorHost.getMainFrame(), "Failed to create documentation merger root node", ex);
            }
        }        
    }//GEN-LAST:event_miSetAsMergeRootActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btDown;
    private javax.swing.JButton btLeft;
    private javax.swing.JButton btRemove;
    private javax.swing.JButton btRight;
    private javax.swing.JButton btUp;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuItem miAssignUnorganizedPage;
    private javax.swing.JMenuItem miCreateNewPage;
    private javax.swing.JMenuItem miOpen;
    private javax.swing.JMenuItem miProperties;
    private javax.swing.JMenuItem miRemove;
    private javax.swing.JMenuItem miSetAsDocXmlRoot;
    private javax.swing.JMenuItem miSetAsMergeRoot;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables

    private void onPopup(MouseEvent evt) {
        TreePath selPath = tree.getPathForLocation(evt.getX(), evt.getY());
            TOCNode node = (TOCNode)selPath.getLastPathComponent();
            
            if (node != toc.getUnorganized() &&
                node != toc.getRoot() &&
                node != toc.getRecycleBin()) {            
                
                if (node.hasTarget()) {
                    miOpen.setEnabled(true);
                    miOpen.setFont(miRemove.getFont().deriveFont(miRemove.getFont().getStyle() | java.awt.Font.BOLD));
                    miProperties.setEnabled(true);
                    miCreateNewPage.setEnabled(false);
                    miCreateNewPage.setFont(miRemove.getFont());
                    miAssignUnorganizedPage.setEnabled(false);
                } else {
                    miOpen.setEnabled(false);
                    miOpen.setFont(miRemove.getFont());
                    miProperties.setEnabled(false);
                    miCreateNewPage.setEnabled(true);  
                    miCreateNewPage.setFont(miRemove.getFont().deriveFont(miRemove.getFont().getStyle() | java.awt.Font.BOLD));
                    miAssignUnorganizedPage.setEnabled(true);
                }

                contextMenuTarget = node;
                popupMenu.show(tree, evt.getX(), evt.getY());
            }
    }

    void selectPage(Page page) {
        
        TOCNode node = toc.findReferenceTo(page);
        if (node != null) {            
            tree.setSelectionPath(new TreePath(node.toPath()));
        }
    }
}
