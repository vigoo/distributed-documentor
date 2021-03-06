package hu.distributeddocumentor.gui;

import java.awt.Frame;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class ErrorDialog extends javax.swing.JDialog {

    public static void show(Frame parent, String title, Exception ex) {
        
        ErrorDialog dlg = new ErrorDialog(parent);
        dlg.setTitle(title);
        dlg.setMessage(ex.getMessage());
        
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        dlg.setCallStack(writer.toString());
        
        dlg.setVisible(true);
    }
    
    public void setMessage(String message) {
        lbMessage.setText(message);
    }
    
    public void setCallStack(String callStack) {
        tbCallStack.setText(callStack);
    }
    
    
    /**
     * Creates new form ErrorDialog
     */
    private ErrorDialog(Frame parent) {
        super(parent, true);
        
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

        icon = new javax.swing.JLabel();
        btOK = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        lbMessage = new com.jidesoft.swing.MultilineLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbCallStack = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/error.png"))); // NOI18N

        btOK.setText("OK");
        btOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOKActionPerformed(evt);
            }
        });

        lbMessage.setColumns(20);
        lbMessage.setRows(1);
        jScrollPane2.setViewportView(lbMessage);

        tbCallStack.setColumns(20);
        tbCallStack.setEditable(false);
        tbCallStack.setRows(5);
        jScrollPane3.setViewportView(tbCallStack);

        jLabel1.setText("Details:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(0, 0, Short.MAX_VALUE)
                .add(btOK))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(icon)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(icon)
                        .add(18, 18, 18))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jLabel1)
                        .add(4, 4, 4)))
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btOK))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOKActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btOKActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btOK;
    private javax.swing.JLabel icon;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea lbMessage;
    private javax.swing.JTextArea tbCallStack;
    // End of variables declaration//GEN-END:variables
}
