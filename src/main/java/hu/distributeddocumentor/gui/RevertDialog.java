package hu.distributeddocumentor.gui;

import hu.distributeddocumentor.controller.CommittableItem;
import hu.distributeddocumentor.controller.CommittableItemsModel;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.FailedToLoadPageException;
import hu.distributeddocumentor.model.FailedToLoadTOCException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;

public class RevertDialog extends javax.swing.JDialog {
    
     /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;
    
    private final Documentation doc;
    private final CommittableItemsModel revertableItems;
    private final PageEditorHost host;
    private int returnStatus = RET_CANCEL;

    /**
     * Gets the dialog's return status 
     * @return RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }        
    
    /**
     * Creates new form RevertDialog
     */
    public RevertDialog(java.awt.Frame parent, Documentation doc, PageEditorHost host) {
        super(parent, true);
                        
        this.doc = doc;
        revertableItems = new CommittableItemsModel(doc);
        
        initComponents();
        
        setLocationRelativeTo(parent);
        this.host = host;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        revertableItemList = new com.jidesoft.swing.CheckBoxList();
        jLabel1 = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        revertableItemList.setModel(revertableItems);
        jScrollPane1.setViewportView(revertableItemList);

        jLabel1.setText("Select the changes you want to revert to their last committed state:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1)
                    .add(layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        getRootPane().setDefaultButton(okButton);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        List<String> selectedItems = new LinkedList<>();
        
        for (Object obj : revertableItemList.getCheckBoxListSelectedValues()) {
            
            CommittableItem item = (CommittableItem)obj;
            selectedItems.add(item.getPath());
        }
        
        try {        
            doc.revertChanges(selectedItems);            
            host.documentationReloaded();
        }
        catch (FailedToLoadPageException | FailedToLoadTOCException ex) {
            org.slf4j.LoggerFactory.getLogger(MainWindow.class.getName()).error(null, ex);
                
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Revert failed", JOptionPane.ERROR_MESSAGE);
        }
        
        returnStatus = RET_OK;
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        returnStatus = RET_CANCEL;
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog
    
    private void doClose() {
        
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    private com.jidesoft.swing.CheckBoxList revertableItemList;
    // End of variables declaration//GEN-END:variables
    
}
