package hu.distributeddocumentor.gui;

import com.google.common.base.Supplier;
import com.jidesoft.popup.JidePopup;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.DocumentWordTokenizer;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import hu.distributeddocumentor.controller.MediaWikiEditor;
import hu.distributeddocumentor.controller.WikiEditor;
import hu.distributeddocumentor.model.IntRange;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class WikiMarkupEditor extends javax.swing.JPanel implements SpellCheckListener {

    private static final Logger log = LoggerFactory.getLogger(WikiMarkupEditor.class.getName());
    
    private final Page page;
    private final DropTarget dropTarget;
    private final UndoManager undoManager = new UndoManager();
    private final PageEditorHost host;        
    private final DocumentorPreferences prefs;
    private WikiEditor editor;
    
    private final Timer spellCheckTimer;
    private boolean isSpellChecking;
    private String lastCheckedMarkup;
    private Map<IntRange, Supplier<List<String>>> suggestions;
        
    private int lastCurrentLine;
    private final PreviewSync previewSync;
    private final HighlightPainter spellCheckHighlightPainter;
    
    
    /**
     * Creates new form WikiMarkupEditor
     */
    public WikiMarkupEditor(final Page page, final PageEditorHost host, PreviewSync previewSync, DocumentorPreferences prefs) {
        initComponents();
        
        this.page = page;
        this.prefs = prefs;        
        
        spellCheckHighlightPainter = new SquiggleUnderlineHighlightPainter(Color.red);
        
        suggestions = new HashMap<>();
                
        updateFont();
                     
        spellCheckTimer = new Timer(2000, 
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        performSpellCheck();
                    }                    
                });
        spellCheckTimer.setRepeats(true);        
        spellCheckTimer.stop();               
        
        syncToPage();
        
        Document document = editorPane.getDocument();
        document.addDocumentListener(
                new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                
                if (!isSpellChecking) {
                    try {
                        page.setMarkup(de.getDocument().getText(0, de.getDocument().getLength()));
                    } catch (BadLocationException ex) {
                        log.error(null, ex);
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                
                if (!isSpellChecking) {
                    try {
                        page.setMarkup(de.getDocument().getText(0, de.getDocument().getLength()));
                    } catch (BadLocationException ex) {
                        log.error(null, ex);
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                
                if (!isSpellChecking) {
                    try {
                        page.setMarkup(de.getDocument().getText(0, de.getDocument().getLength()));
                    } catch (BadLocationException ex) {
                        log.error(null, ex);
                    }
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
        
        InputMap inputMap = editorPane.getInputMap();
        ActionMap actionMap = editorPane.getActionMap();
 
        addUndoAction(actionMap, host, inputMap);
        addRedoAction(actionMap, host, inputMap);
        addFindAction(actionMap, inputMap);        
        addReplaceAction(actionMap, inputMap);
        
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
                                    
                                } catch (UnsupportedFlavorException | IOException | BadLocationException ex) {
                                    log.error(null, ex);
                                }                            
                    }                    
                });
        this.host = host;                
        
        if ("MediaWiki".equals(page.getMarkupLanguage())) {
            editor = new MediaWikiEditor(page);
        }
        this.previewSync = previewSync;                
    }
    
    /**
     * Updates the editor control's font to match the one coming from preferences
     */
    public void updateFont() {
        editorPane.setFont(prefs.getEditorFont());
                
        syncToPage();
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
        editorPane = new javax.swing.JTextPane();
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

        setLayout(new java.awt.BorderLayout());

        editorPane.setFont(new java.awt.Font("Monaco", 0, 13)); // NOI18N
        editorPane.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                editorPaneCaretUpdate(evt);
            }
        });
        editorPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                editorPaneMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                editorPaneMouseReleased(evt);
            }
        });
        editorPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                editorPaneFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                editorPaneFocusLost(evt);
            }
        });
        scrollPane.setViewportView(editorPane);

        add(scrollPane, java.awt.BorderLayout.CENTER);

        toolBar.setFloatable(false);
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

        add(toolBar, java.awt.BorderLayout.PAGE_START);
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

    private void editorPaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_editorPaneFocusGained
        spellCheckTimer.start();
    }//GEN-LAST:event_editorPaneFocusGained

    private void editorPaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_editorPaneFocusLost
        spellCheckTimer.stop();
    }//GEN-LAST:event_editorPaneFocusLost

    private void editorPaneMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editorPaneMousePressed
        
        if (evt.isPopupTrigger()) {
            showSuggestions(evt);
        }             
    }//GEN-LAST:event_editorPaneMousePressed

    private void showSuggestions(MouseEvent evt) {
        int pos = editorPane.viewToModel(new Point(evt.getX(), evt.getY()));
        
        for (final IntRange range : suggestions.keySet()) {
            
            if (range.contains(pos)) {
                
                JPopupMenu popup = new JPopupMenu();
                                                
                List<String> sgs = suggestions.get(range).get();
                for (final String suggestion : sgs) {
                    JMenuItem item = new JMenuItem(suggestion);                        
                    item.addActionListener(
                            new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    editorPane.setSelectionStart(range.getStart());
                                    editorPane.setSelectionEnd(range.getEnd());
                                    editorPane.replaceSelection(suggestion);
                                }
                    });
                    
                    popup.add(item);
                }
                
                popup.show(editorPane, evt.getX(), evt.getY());
            }
        }   
    }
    
    private void editorPaneMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editorPaneMouseReleased
     
         if (evt.isPopupTrigger()) {
            showSuggestions(evt);
         }
    }//GEN-LAST:event_editorPaneMouseReleased

    private void editorPaneCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_editorPaneCaretUpdate
       
        int currentLine = getCurrentRow();
        if (currentLine != lastCurrentLine) {
            
            if (previewSync != null) {
                previewSync.scrollToLine(currentLine);
            }
            
            lastCurrentLine = currentLine;
        }        
    }//GEN-LAST:event_editorPaneCaretUpdate

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
    private javax.swing.JTextPane editorPane;
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
    
    private void removeSpellCheckerHighlights() {
        
        editorPane.getHighlighter().removeAllHighlights();
    }
    
    private void addSpellCheckerHighlight(int start, int end) {
        
        try {
            editorPane.getHighlighter().addHighlight(start, end, spellCheckHighlightPainter);
        } catch (BadLocationException ble) {
            log.error("addSpellCheckerHighlight called with bad location: " + ble.getMessage());
        }
    }
    
    private void performSpellCheck() {

        if (!page.getMarkup().equals(lastCheckedMarkup)) {
            
            lastCheckedMarkup = page.getMarkup();
            suggestions.clear();

            removeSpellCheckerHighlights();

            SpellChecker spellChecker = host.getSpellChecker();
            if (spellChecker != null) {

                log.debug("Starting spell check for " + page.getId());

                synchronized (spellChecker) {

                    isSpellChecking = true;

                    spellChecker.reset();
                    spellChecker.addSpellCheckListener(this);                                 

                    try {
                        spellChecker.checkSpelling(
                                new DocumentWordTokenizer(editorPane.getDocument()));
                    }
                    finally {
                        spellChecker.removeSpellCheckListener(this);
                        isSpellChecking = false;
                    }
                    
                    log.debug("Finished spell check for " + page.getId());
                }
            }
        }
    }

    @Override
    public void spellingError(final SpellCheckEvent event) {
        log.info("Spelling error: ''" + event.getInvalidWord() + "''");
        
        int start = event.getWordContextPosition();
        int length = event.getInvalidWord().length();
        int end = start + length;
        
        addSpellCheckerHighlight(start, end);        
        
        final SpellChecker spellChecker = host.getSpellChecker();
        
        if (spellChecker != null) {
            
            final Supplier<List<String>> suggestionSupplier = new Supplier<List<String>>() {            

                @Override
                public List<String> get() {
                    List wordSuggestions = spellChecker.getSuggestions(event.getInvalidWord(), 0);
                    List<String> sgs = new LinkedList<>();                

                    if (wordSuggestions != null && !wordSuggestions.isEmpty()) {
                        for (Object suggestion : wordSuggestions) {
                            Word word = (Word)suggestion;
                            sgs.add(word.getWord());
                        }
                        
                        return sgs;
                    }
                    else {
                        return new LinkedList<>();
                    }
                }
            };            

            suggestions.put(new IntRange(start, end), suggestionSupplier);
        }
    }

    private void addUndoAction(ActionMap actionMap, final PageEditorHost host, InputMap inputMap) throws HeadlessException {
        actionMap.put("Undo", 
                new AbstractAction("Undo") {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            if (undoManager.canUndo()) {
                                undoManager.undo();
                                host.updateUndoRedoItems();
                            }
                        } catch (CannotUndoException ex) {                        
                            log.warn(null, ex);
                        }
                    }
                });
        // Bind the undo action to ctl-Z (or command-Z on mac)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo" );
    }

    private void addRedoAction(ActionMap actionMap, final PageEditorHost host, InputMap inputMap) throws HeadlessException {
        actionMap.put("Redo", 
                new AbstractAction("Redo") {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        try {
                            if (undoManager.canRedo()) {
                                undoManager.redo();
                                host.updateUndoRedoItems();
                            }
                        } catch (CannotUndoException ex) {                        
                            log.warn(null, ex);
                        }
                    }
                });                
        // Bind the redo action to ctl-Y (or command-Y on mac)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo" );
    }

    private void addFindAction(ActionMap actionMap, InputMap inputMap) {
        actionMap.put("Find", 
                new AbstractAction("Find") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        
                        final FindInTextPanel findPanel = new FindInTextPanel();
                        
                        findPanel.setListener(
                                new FindInTextListener() {

                            @Override
                            public void findNext(String text) {
                                performFindNext(text);                                
                            }

                            @Override
                            public void finish() {
                                remove(findPanel);                                
                                revalidate();
                                repaint();
                                editorPane.requestFocus();
                            }
                        });
                        
                        add(findPanel, BorderLayout.SOUTH);                             
                        revalidate();
                        repaint();
                        findPanel.requestFocus();
                    }
                });
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Find");
    }
    
    private void addReplaceAction(ActionMap actionMap, InputMap inputMap) {
        actionMap.put("Replace", 
                new AbstractAction("Replace") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        final ReplaceInTextPanel replacePanel = new ReplaceInTextPanel();
                        
                        replacePanel.setListener(
                                new ReplaceInTextListener() {

                            @Override
                            public void findNext(String text) {
                                performFindNext(text);                                
                            }

                            @Override
                            public void finish() {
                                remove(replacePanel);                                
                                revalidate();
                                repaint();
                                editorPane.requestFocus();
                            }

                            @Override
                            public boolean hasSelection(String text) {
                                return isSelected(text);
                            }

                            @Override
                            public void replaceCurrent(String text) {
                                performReplace(text);
                            }

                            @Override
                            public void replaceAll(String input, String output) {
                                performReplaceAll(input, output);
                            }
                        });
                        
                        add(replacePanel, BorderLayout.SOUTH);                             
                        revalidate();
                        repaint();
                        replacePanel.requestFocus();
                    }
                });
        
        if (prefs.isWindows()) {
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Replace");
        } else {
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK), "Replace");
        }
    }
    
    private void performFindNext(String text) {
        int startFrom = editorPane.getSelectionEnd();                                
        String fullText = editorPane.getText();
        String remainingText = fullText.substring(startFrom);

        int idx = remainingText.indexOf(text);
        if (idx != -1) {
            editorPane.setSelectionStart(startFrom + idx);
            editorPane.setSelectionEnd(startFrom + idx + text.length());
        }
        else {
            idx = fullText.indexOf(text);
            if (idx != -1) {
                editorPane.setSelectionStart(idx);
                editorPane.setSelectionEnd(idx + text.length());
            }
            else {
                editorPane.setSelectionStart(0);
                editorPane.setSelectionEnd(0);
            }
        }
    }    
    
    private boolean isSelected(String text) {
        return text.equals(editorPane.getSelectedText());
    }
    
    private void performReplace(String newText) {
        String selection = editorPane.getSelectedText();
        editorPane.replaceSelection(newText);
        performFindNext(selection);
    }
    
    private void performReplaceAll(String oldText, String newText) {
        String markup = page.getMarkup();
        page.setMarkup(markup.replaceAll(oldText, newText));
        syncToPage();
    }
}
