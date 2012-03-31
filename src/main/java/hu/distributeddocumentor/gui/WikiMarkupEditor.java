package hu.distributeddocumentor.gui;

import com.jidesoft.popup.JidePopup;
import hu.distributeddocumentor.controller.MediaWikiEditor;
import hu.distributeddocumentor.controller.WikiEditor;
import hu.distributeddocumentor.model.Page;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author vigoo
 */
public class WikiMarkupEditor extends javax.swing.JPanel {

    private final Page page;
    private final DropTarget dropTarget;
    private final UndoManager undoManager = new UndoManager();
    private final PageEditorHost host;
    private WikiEditor editor;
    
    
    /**
     * Creates new form WikiMarkupEditor
     */
    public WikiMarkupEditor(final Page page, final PageEditorHost host) {
        initComponents();
        
        this.page = page;
        
        syncToPage();
        
        Document document = editorPane.getDocument();
        document.addDocumentListener(
                new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                try {
                    page.setMarkup(de.getDocument().getText(0, de.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    Logger.getLogger(WikiMarkupEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                try {
                    page.setMarkup(de.getDocument().getText(0, de.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    Logger.getLogger(WikiMarkupEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                try {
                    page.setMarkup(de.getDocument().getText(0, de.getDocument().getLength()));
                } catch (BadLocationException ex) {
                    Logger.getLogger(WikiMarkupEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        document.addUndoableEditListener(
                new UndoableEditListener() {

            @Override
            public void undoableEditHappened(UndoableEditEvent uee) {
                undoManager.addEdit(uee.getEdit());
                host.updateUndoRedoItems();
            }
        });
        
        editorPane.getActionMap().put("Undo", 
                new AbstractAction("Undo") {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            if (undoManager.canUndo()) {
                                undoManager.undo();
                                host.updateUndoRedoItems();
                            }
                        } catch (CannotUndoException ex) {                        
                            Logger.getLogger(WikiMarkupEditor.class.getName()).log(Level.WARNING, null, ex);
                        }
                    }
                });
        
        editorPane.getActionMap().put("Redo", 
                new AbstractAction("Redo") {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            if (undoManager.canRedo()) {
                                undoManager.redo();
                                host.updateUndoRedoItems();
                            }
                        } catch (CannotUndoException ex) {                        
                            Logger.getLogger(WikiMarkupEditor.class.getName()).log(Level.WARNING, null, ex);
                        }
                    }
                });
        
        // Bind the undo action to ctl-Z (or command-Z on mac)
        editorPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );
        // Bind the redo action to ctl-Y (or command-Y on mac)
        editorPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );
        
        dropTarget = new DropTarget(editorPane, 
                new DropTargetListener() {

                    @Override
                    public void dragEnter(DropTargetDragEvent dtde) {
                    }

                    @Override
                    public void dragOver(DropTargetDragEvent dtde) {
                    }

                    @Override
                    public void dropActionChanged(DropTargetDragEvent dtde) {
                    }

                    @Override
                    public void dragExit(DropTargetEvent dte) {
                    }

                    @Override
                    public void drop(DropTargetDropEvent dtde) {
                        
                        Transferable transferable = dtde.getTransferable();
                                dtde.acceptDrop(DnDConstants.ACTION_LINK);
                                
                                try {
                                    String str = (String)transferable.getTransferData(DataFlavor.stringFlavor);
                                    
                                    int pos = editorPane.viewToModel(dtde.getLocation());
                                    editorPane.getDocument().insertString(pos, str, null);
                                    
                                } catch (UnsupportedFlavorException ex) {
                                    Logger.getLogger(WikiMarkupEditor.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IOException ex) {
                                    Logger.getLogger(WikiMarkupEditor.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (BadLocationException ex) {
                                    Logger.getLogger(WikiMarkupEditor.class.getName()).log(Level.SEVERE, null, ex);
                                }                            
                    }                    
                });
        this.host = host;
        
        if ("MediaWiki".equals(page.getMarkupLanguage()))
            editor = new MediaWikiEditor(page);
    }

    /*
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        toolBar = new javax.swing.JToolBar();
        btH1 = new javax.swing.JButton();
        btH2 = new javax.swing.JButton();
        btH3 = new javax.swing.JButton();
        btH4 = new javax.swing.JButton();
        btH5 = new javax.swing.JButton();
        btH6 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btBold = new javax.swing.JButton();
        btItalic = new javax.swing.JButton();
        btColor = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btBullets = new javax.swing.JButton();
        btEnumeration = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btUnindent = new javax.swing.JButton();
        btIndent = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        btAddRemoteLink = new javax.swing.JButton();

        editorPane.setFont(new java.awt.Font("Monaco", 0, 13)); // NOI18N
        scrollPane.setViewportView(editorPane);

        toolBar.setRollover(true);

        btH1.setText("H1");
        btH1.setToolTipText("Sets the current line to be first level heading");
        btH1.setFocusable(false);
        btH1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btH1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btH1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btH1ActionPerformed(evt);
            }
        });
        toolBar.add(btH1);

        btH2.setText("H2");
        btH2.setToolTipText("Sets the current line to be second level heading");
        btH2.setFocusable(false);
        btH2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btH2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btH2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btH2ActionPerformed(evt);
            }
        });
        toolBar.add(btH2);

        btH3.setText("H3");
        btH3.setToolTipText("Sets the current line to third level heading");
        btH3.setFocusable(false);
        btH3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btH3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btH3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btH3ActionPerformed(evt);
            }
        });
        toolBar.add(btH3);

        btH4.setText("H4");
        btH4.setToolTipText("Sets the current line to fourth level heading");
        btH4.setFocusable(false);
        btH4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btH4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btH4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btH4ActionPerformed(evt);
            }
        });
        toolBar.add(btH4);

        btH5.setText("H5");
        btH5.setToolTipText("Sets the current line to fifth level heading");
        btH5.setFocusable(false);
        btH5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btH5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btH5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btH5ActionPerformed(evt);
            }
        });
        toolBar.add(btH5);

        btH6.setText("H6");
        btH6.setToolTipText("Sets the current line to sixth level heading");
        btH6.setFocusable(false);
        btH6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btH6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btH6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btH6ActionPerformed(evt);
            }
        });
        toolBar.add(btH6);
        toolBar.add(jSeparator1);

        btBold.setText("B");
        btBold.setToolTipText("Sets the selected string to bold");
        btBold.setFocusable(false);
        btBold.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btBold.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btBold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBoldActionPerformed(evt);
            }
        });
        toolBar.add(btBold);

        btItalic.setText("I");
        btItalic.setToolTipText("Sets the selected string to italic");
        btItalic.setFocusable(false);
        btItalic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btItalic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btItalic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btItalicActionPerformed(evt);
            }
        });
        toolBar.add(btItalic);

        btColor.setText("Color");
        btColor.setToolTipText("Sets the selected text to a different color");
        btColor.setFocusable(false);
        btColor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btColor.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btColorActionPerformed(evt);
            }
        });
        toolBar.add(btColor);
        toolBar.add(jSeparator2);

        btBullets.setText("*");
        btBullets.setToolTipText("Sets the selected lines to represent a bulleted list");
        btBullets.setFocusable(false);
        btBullets.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btBullets.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btBullets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBulletsActionPerformed(evt);
            }
        });
        toolBar.add(btBullets);

        btEnumeration.setText("1.");
        btEnumeration.setToolTipText("Sets the selected lines to represent an enumeration.");
        btEnumeration.setFocusable(false);
        btEnumeration.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btEnumeration.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btEnumeration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEnumerationActionPerformed(evt);
            }
        });
        toolBar.add(btEnumeration);
        toolBar.add(jSeparator3);

        btUnindent.setText("<");
        btUnindent.setToolTipText("Unindent the selected lines");
        btUnindent.setFocusable(false);
        btUnindent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btUnindent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btUnindent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btUnindentActionPerformed(evt);
            }
        });
        toolBar.add(btUnindent);

        btIndent.setText(">");
        btIndent.setToolTipText("Indents the selected lines.");
        btIndent.setFocusable(false);
        btIndent.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btIndent.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btIndent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btIndentActionPerformed(evt);
            }
        });
        toolBar.add(btIndent);
        toolBar.add(jSeparator4);

        btAddRemoteLink.setText("url");
        btAddRemoteLink.setFocusable(false);
        btAddRemoteLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btAddRemoteLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btAddRemoteLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddRemoteLinkActionPerformed(evt);
            }
        });
        toolBar.add(btAddRemoteLink);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(scrollPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(toolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private int getCurrentRow() {
        return getRow(editorPane.getCaret().getDot());        
    }
    
    private int getRow(int dot) {
        return editorPane.getDocument().getDefaultRootElement().getElementIndex(dot);
    }
    
    private void btH1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btH1ActionPerformed
               
        editor.setHeadingLevel(getCurrentRow(), 1);
        syncToPage();
    }//GEN-LAST:event_btH1ActionPerformed

    private void btH2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btH2ActionPerformed
        
        editor.setHeadingLevel(getCurrentRow(), 2);
        syncToPage();
    }//GEN-LAST:event_btH2ActionPerformed

    private void btH3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btH3ActionPerformed
        
        editor.setHeadingLevel(getCurrentRow(), 3);
        syncToPage();
    }//GEN-LAST:event_btH3ActionPerformed

    private void btH4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btH4ActionPerformed
        
        editor.setHeadingLevel(getCurrentRow(), 4);
        syncToPage();
    }//GEN-LAST:event_btH4ActionPerformed

    private void btH5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btH5ActionPerformed
        
        editor.setHeadingLevel(getCurrentRow(), 5);
        syncToPage();
    }//GEN-LAST:event_btH5ActionPerformed

    private void btH6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btH6ActionPerformed
       
        editor.setHeadingLevel(getCurrentRow(), 6);
        syncToPage();
    }//GEN-LAST:event_btH6ActionPerformed

    private void btBoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBoldActionPerformed
                
        int start = editorPane.getSelectionStart();        
        int end = editorPane.getSelectionEnd();
        
        editor.setBold(start, end);
        syncToPage();
    }//GEN-LAST:event_btBoldActionPerformed

    private void btItalicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btItalicActionPerformed
        
        int start = editorPane.getSelectionStart();        
        int end = editorPane.getSelectionEnd();
        
        editor.setItalic(start, end);
        syncToPage();
    }//GEN-LAST:event_btItalicActionPerformed

    private void btColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btColorActionPerformed
        
        final JidePopup popup = new JidePopup();        
        final JColorChooser chooser = new JColorChooser();
        popup.add(chooser);
        
        chooser.getSelectionModel().addChangeListener(
                new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent ce) {
                        popup.hidePopup();
                        
                        int start = editorPane.getSelectionStart();        
                        int end = editorPane.getSelectionEnd();

                        editor.setColor(start, end, chooser.getColor());
                        syncToPage();
                    }                    
                });
        
        popup.setFocusable(true);
        popup.showPopup(btColor);                                
    }//GEN-LAST:event_btColorActionPerformed

    private void btBulletsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBulletsActionPerformed
        int startRow = getRow(editorPane.getSelectionStart());
        int endRow = getRow(editorPane.getSelectionEnd());
        
        editor.toggleBullets(startRow, endRow);
        syncToPage();
    }//GEN-LAST:event_btBulletsActionPerformed

    private void btEnumerationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEnumerationActionPerformed
        int startRow = getRow(editorPane.getSelectionStart());
        int endRow = getRow(editorPane.getSelectionEnd());
        
        editor.toggleEnumeration(startRow, endRow);
        syncToPage();
    }//GEN-LAST:event_btEnumerationActionPerformed

    private void btUnindentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btUnindentActionPerformed
        int startRow = getRow(editorPane.getSelectionStart());
        int endRow = getRow(editorPane.getSelectionEnd());
        
        editor.unindent(startRow, endRow);
        syncToPage();
    }//GEN-LAST:event_btUnindentActionPerformed

    private void btIndentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btIndentActionPerformed
        int startRow = getRow(editorPane.getSelectionStart());
        int endRow = getRow(editorPane.getSelectionEnd());
        
        editor.indent(startRow, endRow);
        syncToPage();
    }//GEN-LAST:event_btIndentActionPerformed

    private void btAddRemoteLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddRemoteLinkActionPerformed
        
        CreateRemoteLinkDialog dlg = new CreateRemoteLinkDialog(host.getMainFrame(), true);
        dlg.setVisible(true);
        
        if (dlg.getReturnStatus() == CreateRemoteLinkDialog.RET_OK) {
            
            editor.insertRemoteLink(editorPane.getCaret().getDot(), 
                                    dlg.getURL(), dlg.getLinkText());
            syncToPage();
        }
    }//GEN-LAST:event_btAddRemoteLinkActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddRemoteLink;
    private javax.swing.JButton btBold;
    private javax.swing.JButton btBullets;
    private javax.swing.JButton btColor;
    private javax.swing.JButton btEnumeration;
    private javax.swing.JButton btH1;
    private javax.swing.JButton btH2;
    private javax.swing.JButton btH3;
    private javax.swing.JButton btH4;
    private javax.swing.JButton btH5;
    private javax.swing.JButton btH6;
    private javax.swing.JButton btIndent;
    private javax.swing.JButton btItalic;
    private javax.swing.JButton btUnindent;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    private void syncToPage() {
        
        int dot = editorPane.getCaret().getDot();
        editorPane.setText(page.getMarkup());
        editorPane.getCaret().setDot(dot);
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }
    
}
