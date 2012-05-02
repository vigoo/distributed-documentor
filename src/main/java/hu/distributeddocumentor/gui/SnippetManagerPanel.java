package hu.distributeddocumentor.gui;

import hu.distributeddocumentor.controller.SnippetListModel;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.Snippet;
import hu.distributeddocumentor.utils.StringUtils;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;


public class SnippetManagerPanel extends javax.swing.JPanel {
    
    private final PageEditorHost host;
    private final Documentation doc;
    private final SnippetListModel snippetModel;

    /**
     * Creates new form SnippetManagerPanel
     */
    public SnippetManagerPanel(PageEditorHost host, Documentation doc) {
        
        this.host = host;
        this.doc = doc;
        
        initComponents();      
        
        snippetModel = new SnippetListModel(doc);
        
        snippetTable.setModel(snippetModel);
        snippetTable.setTransferHandler(
        new TransferHandler() {

            @Override
            protected Transferable createTransferable(JComponent jc) {

                JTable table = (JTable)jc;
                
                int row = table.getSelectedRow();
                String item = (String)table.getValueAt(row, 0);

                return new StringSelection("\n[Snippet:" + StringUtils.convertSpaces(item) + "]\n");
            }

            @Override
            public int getSourceActions(JComponent jc) {
                return LINK;
            }

        });
        
        snippetTable.getRowSorter().toggleSortOrder(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btRemove = new javax.swing.JButton();
        btAdd = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        snippetTable = new javax.swing.JTable();

        btRemove.setText("Remove");
        btRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveActionPerformed(evt);
            }
        });

        btAdd.setText("Add...");
        btAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddActionPerformed(evt);
            }
        });

        snippetTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        snippetTable.setAutoCreateRowSorter(true);
        snippetTable.setDragEnabled(true);
        snippetTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        snippetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                snippetTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(snippetTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(btAdd)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 38, Short.MAX_VALUE)
                .add(btRemove))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btAdd)
                    .add(btRemove)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveActionPerformed

        int row = snippetTable.getSelectedRow();
        String item = (String)snippetTable.getValueAt(row, 0);
        
        doc.removeSnippet(item);

    }//GEN-LAST:event_btRemoveActionPerformed

    private void btAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddActionPerformed

       CreateNewPageDialog dlg = new CreateNewPageDialog(host.getMainFrame(), true, doc, "");
       dlg.setVisible(true);
       
       if (dlg.getReturnStatus() == CreateNewPageDialog.RET_OK) {
           
           try {
            Snippet snippet = new Snippet(dlg.getID(), doc);
            doc.addSnippet(snippet);
           }
           catch (Exception ex) {
               ErrorDialog.show(null, "Failed to add new snippet", ex);
           }
       }
    }//GEN-LAST:event_btAddActionPerformed

    private void snippetTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_snippetTableMouseClicked
         
        if (evt.getClickCount() == 2) {
            
            int index = snippetTable.rowAtPoint(evt.getPoint());            
            String id = (String) snippetTable.getValueAt(index, 0);
            
            host.openOrFocusSnippet(id);
        }     
    }//GEN-LAST:event_snippetTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btRemove;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable snippetTable;
    // End of variables declaration//GEN-END:variables
}
