/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.distributeddocumentor.gui;

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
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
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

        editorPane.setFont(new java.awt.Font("Monaco", 0, 13)); // NOI18N
        scrollPane.setViewportView(editorPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(scrollPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    private void syncToPage() {
        editorPane.setText(page.getMarkup());
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }
    
}
