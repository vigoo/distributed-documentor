
package hu.distributeddocumentor.gui;

import hu.distributeddocumentor.model.Documentation;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author vigoo
 */
public class StartupDialog extends javax.swing.JDialog {
    
    private enum Action {
        CreateNew,
        OpenLocal,
        OpenRemote
    }
    
    private Action finalAction;
    private File repositoryRoot;

    /**
     * Creates new form StartupDialog
     */
    public StartupDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        setLocationRelativeTo(parent);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCreateNew = new javax.swing.JButton();
        btnOpenLocal = new javax.swing.JButton();
        btnOpenRemote = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        btnCreateNew.setText("Create new");
        btnCreateNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateNewActionPerformed(evt);
            }
        });

        btnOpenLocal.setText("<html><p align=\"center\">Open from<br/>local directory</p></html>");
        btnOpenLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenLocalActionPerformed(evt);
            }
        });

        btnOpenRemote.setText("<html><p align=\"center\">Clone from<br/>remote repository</p></html>");
        btnOpenRemote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenRemoteActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Welcome to Distributed Documentor!");

        jLabel2.setText("Please choose a documentation to work with:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(btnCreateNew, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnOpenLocal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 162, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(btnOpenRemote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 169, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {btnCreateNew, btnOpenLocal, btnOpenRemote}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 26, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(btnCreateNew, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(btnOpenRemote, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                        .add(btnOpenLocal)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {btnCreateNew, btnOpenLocal, btnOpenRemote}, org.jdesktop.layout.GroupLayout.VERTICAL);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    private void btnOpenLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenLocalActionPerformed

        finalAction = Action.OpenLocal;
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select the root directory");
                
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            repositoryRoot = chooser.getSelectedFile();
            
            doClose();
        }                  
    }//GEN-LAST:event_btnOpenLocalActionPerformed

    private void btnCreateNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateNewActionPerformed
        
        finalAction = Action.CreateNew;
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select the root directory");
                
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            repositoryRoot = chooser.getSelectedFile();
            
            doClose();
        }                        
    }//GEN-LAST:event_btnCreateNewActionPerformed

    private void btnOpenRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenRemoteActionPerformed
        
        doClose();
    }//GEN-LAST:event_btnOpenRemoteActionPerformed
    
    private void doClose() {
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCreateNew;
    private javax.swing.JButton btnOpenLocal;
    private javax.swing.JButton btnOpenRemote;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    void initialize(Documentation doc) {
        
        try {
            switch (finalAction) {
                case CreateNew:
                    doc.initAsNew(repositoryRoot);
                    
                    break;
                case OpenLocal:
                    doc.initFromExisting(repositoryRoot);    
                    
                    break;
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(StartupDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Failed to load documentation", JOptionPane.ERROR_MESSAGE);
        }
    }
}
