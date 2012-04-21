package hu.distributeddocumentor.gui;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;
import hu.distributeddocumentor.controller.sync.DialogBasedSyncInteraction;
import hu.distributeddocumentor.controller.sync.MercurialSync;
import hu.distributeddocumentor.controller.sync.SyncController;
import hu.distributeddocumentor.exporters.CHMExporter;
import hu.distributeddocumentor.exporters.Exporter;
import hu.distributeddocumentor.exporters.HTMLExporter;
import hu.distributeddocumentor.model.CouldNotSaveDocumentationException;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import org.noos.xing.mydoggy.*;
import org.noos.xing.mydoggy.event.ContentManagerEvent;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyTabbedContentManagerUI;

public final class MainWindow extends javax.swing.JFrame implements PageEditorHost, ContentManagerListener {

    private final DocumentorPreferences prefs;
    private final Documentation doc;
    private final MyDoggyToolWindowManager toolWindowManager;
    private final Timer saveTimer;
    private final Timer statusCheckTimer;
    private final Timer removeOrphanedPagesTimer;
    private UndoManager currentUndoManager;
    private SpellChecker spellChecker;

    @Override
    public SpellChecker getSpellChecker() {
        return spellChecker;
    }        
    
    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        setSize(1024, 768);
        
        try {
            SpellDictionary dictionary = new SpellDictionaryHashMap(
                    new BufferedReader(
                       new InputStreamReader(WikiMarkupEditor.class.getResourceAsStream("/dict/en.txt"))));
            spellChecker = new SpellChecker(dictionary);        
        }
        catch (Exception ex) {
            spellChecker = null;
            ErrorDialog.show(this, "Failed to initialize spell checker", ex);            
        }
        
        prefs = new DocumentorPreferences();
        doc = new Documentation(prefs);
        
        toolWindowManager = new MyDoggyToolWindowManager();
        ContentManager contentManager = toolWindowManager.getContentManager();
        MyDoggyTabbedContentManagerUI contentManagerUI = new MyDoggyTabbedContentManagerUI();                        
        contentManager.setContentManagerUI(contentManagerUI);       
        
        contentManagerUI.setShowAlwaysTab(true);        
        contentManager.addContentManagerListener(this);
       
        add(toolWindowManager, BorderLayout.CENTER);
                
        ToolWindow twTOC = toolWindowManager.registerToolWindow(
                "TOC", "Table of contents", null, 
                new TableOfContentsView(doc, this), 
                ToolWindowAnchor.LEFT);
                
        twTOC.setType(ToolWindowType.DOCKED);
        twTOC.setAutoHide(false);
        twTOC.setVisible(true);
        twTOC.setAvailable(true);        
        
        showPreferencesIfNecessary();
            
        final StartupDialog startup = new StartupDialog(this, prefs);
        startup.setVisible(true);
        
        boolean loaded = false;
        if (startup.getFinalAction() != StartupDialog.Action.Cancel) {
            if (startup.initialize(doc)) {

                ToolWindow twImages = toolWindowManager.registerToolWindow(
                        "IMG", 
                        "Image manager", 
                        null, 
                        new ImageManagerPanel(doc.getImages()), 
                        ToolWindowAnchor.LEFT);
                twImages.setType(ToolWindowType.DOCKED);
                twImages.setAutoHide(false);
                twImages.setVisible(true);
                twImages.setAvailable(true);                                

                ToolWindow twSnippets = toolWindowManager.registerToolWindow(
                        "SNIP",
                        "Snippets manager",
                        null,
                        new SnippetManagerPanel(this, doc),
                        ToolWindowAnchor.RIGHT);
                twSnippets.setType(ToolWindowType.DOCKED);
                twSnippets.setAutoHide(false);
                twSnippets.setVisible(true);
                twSnippets.setAvailable(true);
                
                labelRoot.setText(doc.getRepositoryRoot());

                openOrFocusPage("start");

                setVisible(true);        
                loadLayout();

                saveTimer = new Timer(1000, 
                        new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                onSaveTimerTick();
                            }                    
                        });  
                saveTimer.setInitialDelay(5000);
                saveTimer.start();

                statusCheckTimer = new Timer(3000,
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                onStatusCheckTimerTick();
                            }
                        });
                statusCheckTimer.setInitialDelay(0);
                statusCheckTimer.start();       

                removeOrphanedPagesTimer = new Timer(2000,
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                onRemoveOrphanedPagesTimerTick();
                            }
                        });
                removeOrphanedPagesTimer.setInitialDelay(0);
                removeOrphanedPagesTimer.start();
                
                loaded = true;
            } else{            
                saveTimer = statusCheckTimer = removeOrphanedPagesTimer = null;
            }
        } else {            
                saveTimer = statusCheckTimer = removeOrphanedPagesTimer = null;
        }
        
        if (!loaded)
        {            
            System.exit(0);
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

        toolBar = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        labelRoot = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        labelUncommitted = new javax.swing.JLabel();
        btCommit = new javax.swing.JButton();
        btRevert = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exportMenu = new javax.swing.JMenu();
        exportToCHMMenuItem = new javax.swing.JMenuItem();
        exportToHTMLMenuItem = new javax.swing.JMenuItem();
        preferencesMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        synchronizeMenu = new javax.swing.JMenu();
        pullMenuItem = new javax.swing.JMenuItem();
        pushMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        userManualItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Distributed Documentor");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        toolBar.setRollover(true);

        jLabel1.setText("Documentation root: ");
        toolBar.add(jLabel1);

        labelRoot.setFont(labelRoot.getFont().deriveFont(labelRoot.getFont().getStyle() | java.awt.Font.BOLD));
        labelRoot.setText("none");
        toolBar.add(labelRoot);
        toolBar.add(filler1);

        labelUncommitted.setFont(labelUncommitted.getFont().deriveFont(labelUncommitted.getFont().getStyle() | java.awt.Font.BOLD));
        labelUncommitted.setForeground(new java.awt.Color(255, 0, 0));
        labelUncommitted.setText("There are uncommitted changes!");
        toolBar.add(labelUncommitted);

        btCommit.setText("Commit!");
        btCommit.setFocusable(false);
        btCommit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btCommit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCommitActionPerformed(evt);
            }
        });
        toolBar.add(btCommit);

        btRevert.setText("Revert");
        btRevert.setFocusable(false);
        btRevert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btRevert.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btRevert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRevertActionPerformed(evt);
            }
        });
        toolBar.add(btRevert);

        getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        exportMenu.setText("Export");

        exportToCHMMenuItem.setText("Export to CHM...");
        exportToCHMMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToCHMMenuItemActionPerformed(evt);
            }
        });
        exportMenu.add(exportToCHMMenuItem);

        exportToHTMLMenuItem.setText("Export to HTML...");
        exportToHTMLMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportToHTMLMenuItemActionPerformed(evt);
            }
        });
        exportMenu.add(exportToHTMLMenuItem);

        fileMenu.add(exportMenu);

        preferencesMenuItem.setText("Preferences...");
        preferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(preferencesMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");

        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        undoMenuItem.setText("Undo");
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        redoMenuItem.setText("Redo");
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(redoMenuItem);

        menuBar.add(editMenu);

        synchronizeMenu.setText("Synchronize");

        pullMenuItem.setText("Download changes");
        pullMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pullMenuItemActionPerformed(evt);
            }
        });
        synchronizeMenu.add(pullMenuItem);

        pushMenuItem.setText("Upload changes");
        pushMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pushMenuItemActionPerformed(evt);
            }
        });
        synchronizeMenu.add(pushMenuItem);

        menuBar.add(synchronizeMenu);

        helpMenu.setText("Help");

        userManualItem.setText("Online user manual...");
        userManualItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userManualItemActionPerformed(evt);
            }
        });
        helpMenu.add(userManualItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        
        saveLayout();
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        saveLayout();
    }

    private File getWorkspaceFile() {
        return new File(System.getProperty("user.home"), "documentor.workspace.xml");
    }
    
    private void saveLayout() {
        PersistenceDelegate delegate = toolWindowManager.getPersistenceDelegate();
        try {
            // TODO: better location for this file
            FileOutputStream output = new FileOutputStream(getWorkspaceFile());
            delegate.save(output);
            output.close();        
        } catch (Exception e) {
            // TODO
        }        
    }//GEN-LAST:event_formWindowClosing

    private void btCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCommitActionPerformed
        
        final CommitDialog dlg = new CommitDialog(this, doc);
        dlg.setVisible(true);
    }//GEN-LAST:event_btCommitActionPerformed

    private void btRevertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRevertActionPerformed
        
        final RevertDialog dlg = new RevertDialog(this, doc, this);
        dlg.setVisible(true);                
    }//GEN-LAST:event_btRevertActionPerformed

    private void exportToCHMMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToCHMMenuItemActionPerformed
       
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select the target directory");
                
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetDir = chooser.getSelectedFile();
            Exporter exporter = new CHMExporter(prefs, doc, targetDir);
            
            try {
                exporter.export();
            }
            catch (Exception ex) {
                java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE);
            }
        }  
    }//GEN-LAST:event_exportToCHMMenuItemActionPerformed

    private void preferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesMenuItemActionPerformed
        showPreferences();        
    }//GEN-LAST:event_preferencesMenuItemActionPerformed

    private void exportToHTMLMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToHTMLMenuItemActionPerformed
        
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select the target directory");
                
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetDir = chooser.getSelectedFile();
            Exporter exporter = new HTMLExporter(prefs, doc, targetDir);
            
            try {
                exporter.export();
            }
            catch (Exception ex) {
                java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                
                ErrorDialog.show(this, "Export failed", ex);
            }
        }  
    }//GEN-LAST:event_exportToHTMLMenuItemActionPerformed

    private void pullMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pullMenuItemActionPerformed
        
        try {
            SyncController controller = createSyncConrtoller();
            controller.pull();
        }
        catch (Exception ex) {
            ErrorDialog.show(this, "Failed to download changes", ex);
        }
    }//GEN-LAST:event_pullMenuItemActionPerformed

    private void pushMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pushMenuItemActionPerformed
        
        try {
            SyncController controller = createSyncConrtoller();
            controller.push();        
        }
        catch (Exception ex) {
            ErrorDialog.show(this, "Failed to upload changes", ex);
        }
    }//GEN-LAST:event_pushMenuItemActionPerformed

    private void userManualItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userManualItemActionPerformed
       
        try {
            Desktop.getDesktop().browse(new URI("http://freezingmoon.dyndns.org/"));
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }                    
    }//GEN-LAST:event_userManualItemActionPerformed

    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        
        if (currentUndoManager != null) {
            currentUndoManager.undo();
            updateUndoRedoItems();
        }
    }//GEN-LAST:event_undoMenuItemActionPerformed

    private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
        
        if (currentUndoManager != null) {
            currentUndoManager.redo();
            updateUndoRedoItems();
        }
    }//GEN-LAST:event_redoMenuItemActionPerformed

    private void showPreferencesIfNecessary() {
        
        if (!prefs.hasValidMercurialPath()) {
            showPreferences();
        }
    }
    
    private void showPreferences() {
        SettingsDialog dlg = new SettingsDialog(this, true, prefs);
        dlg.setVisible(true);        
    }
    
    private void loadLayout() {
        
        File workspaceFile = new File(System.getProperty("user.home"), "documentor.workspace.xml");
        
        if (workspaceFile.exists()) {        
            PersistenceDelegate delegate = toolWindowManager.getPersistenceDelegate();
            try {
                // TODO: better location for this file
                FileInputStream input = new FileInputStream(workspaceFile);
                delegate.merge(input, PersistenceDelegate.MergePolicy.RESET);
                input.close();        
            } catch (Exception e) {
                // TODO
            }  
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        System.setProperty("apple.laf.useScreenMenuBar", "true");        
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Distributed Documentor");
                
        NativeInterface.open();
        
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        LookAndFeelFactory.installJideExtension( LookAndFeelFactory.VSNET_STYLE_WITHOUT_MENU);  
        
        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                java.awt.EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
                queue.push(new EventQueueProxy());
                
                new MainWindow().setVisible(true);
            }
        });
        
        NativeInterface.runEventPump();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCommit;
    private javax.swing.JButton btRevert;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu exportMenu;
    private javax.swing.JMenuItem exportToCHMMenuItem;
    private javax.swing.JMenuItem exportToHTMLMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel labelRoot;
    private javax.swing.JLabel labelUncommitted;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JMenuItem pullMenuItem;
    private javax.swing.JMenuItem pushMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenu synchronizeMenu;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenuItem userManualItem;
    // End of variables declaration//GEN-END:variables


    @Override
    public void openOrFocusPage(String id) {
                
        ContentManager contentManager = toolWindowManager.getContentManager();
        
        Content content = contentManager.getContent(id);
        if (content == null) {            
            content = contentManager.addContent(
                id,
                "Page: " + id,
                null,
                new SplittedPageView(doc.getPage(id), new File(doc.getRepositoryRoot()), this));   
        }
         
        content.setSelected(true);        
    }    

    @Override
    public void openOrFocusSnippet(String id) {
        
        ContentManager contentManager = toolWindowManager.getContentManager();
        String contentId = "Snippet:"+id;
        
        Content content = contentManager.getContent(id);
        if (content == null) {            
            content = contentManager.addContent(
                contentId,
                "Snippet: " + id,
                null,
                new SplittedPageView(doc.getSnippet(id), new File(doc.getRepositoryRoot()), this));   
        }
         
        content.setSelected(true); 
    }    
    
    private void onSaveTimerTick() {
        try {
            doc.saveAll();
        }
        catch (CouldNotSaveDocumentationException ex) {
            // TODO
        }
    }
    
    private void onStatusCheckTimerTick() {
        
        boolean hasChanges = doc.hasChanges();
        
        labelUncommitted.setVisible(hasChanges);
        btCommit.setVisible(hasChanges);
        btRevert.setVisible(hasChanges);
        
    }
    
    private void onRemoveOrphanedPagesTimerTick() {
        doc.processOrphanedPages();
    }

    @Override
    public void documentationReloaded() {
        
        // If this method is called, it means that the documentation model has
        // been completely reloaded. Page and TOCNode objects are no longer alive
        // so we have to close every opened page and regenerate the TOC and image
        // lists.
        
        // 1. Closing the pages
        ContentManager contentManager = toolWindowManager.getContentManager();
        contentManager.removeAllContents();
                
        // 2. Image panel is updated automatically through the observable pattern
        // 3. TOC tree is updated automatically through the tree model listeners
        
    }

    @Override
    public Frame getMainFrame() {
        return this;
    }

    @Override
    public void contentAdded(ContentManagerEvent cme) {
    }

    @Override
    public void contentRemoved(ContentManagerEvent cme) {
        
        SplittedPageView view = (SplittedPageView)cme.getContent().getComponent();
        view.dispose();
    }

    @Override
    public void contentSelected(ContentManagerEvent cme) {
        
        Component comp = cme.getContent().getComponent();;
        if (comp instanceof SplittedPageView) {
            SplittedPageView view = (SplittedPageView)comp;
            
            currentUndoManager = view.getEditorUndoManager();            
        } else {
            currentUndoManager = null;
        }        
        
        updateUndoRedoItems();
    }
    
    @Override
    public void updateUndoRedoItems() {
        
        boolean canUndo = currentUndoManager != null && currentUndoManager.canUndo();
        boolean canRedo = currentUndoManager != null && currentUndoManager.canRedo();
        
        undoMenuItem.setEnabled(canUndo);
        redoMenuItem.setEnabled(canRedo);
    }

    private SyncController createSyncConrtoller() {
        MercurialSync hg = new MercurialSync(doc, prefs);
        DialogBasedSyncInteraction dlgui = new DialogBasedSyncInteraction(this, doc);
        SyncController controller = new SyncController(hg, hg, hg, dlgui, doc, this);
        return controller;
    }

}
