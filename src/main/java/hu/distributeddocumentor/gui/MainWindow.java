package hu.distributeddocumentor.gui;

import com.google.common.io.Files;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;
import hu.distributeddocumentor.controller.CommandLineExporter;
import hu.distributeddocumentor.controller.sync.DialogBasedSyncInteraction;
import hu.distributeddocumentor.controller.sync.MercurialSync;
import hu.distributeddocumentor.controller.sync.SyncController;
import hu.distributeddocumentor.model.CouldNotSaveDocumentationException;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.FailedToLoadMetadataException;
import hu.distributeddocumentor.model.FailedToLoadPageException;
import hu.distributeddocumentor.model.FailedToLoadTOCException;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.Snippet;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.LinkedList;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.undo.UndoManager;
import org.apache.log4j.PropertyConfigurator;
import org.noos.xing.mydoggy.*;
import org.noos.xing.mydoggy.event.ContentManagerEvent;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.content.MyDoggyTabbedContentManagerUI;
import org.noos.xing.mydoggy.plaf.ui.util.SwingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MainWindow extends javax.swing.JFrame implements PageEditorHost, ContentManagerListener {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class.getName());
    private final DocumentorPreferences prefs;
    private final Documentation doc;
    private final MyDoggyToolWindowManager toolWindowManager;
    private final Timer saveTimer;
    private final Timer statusCheckTimer;
    private final Timer removeOrphanedPagesTimer;
    private final FloatingPreview floatingPreview;
    private SpellChecker spellChecker;

    private UndoManager currentUndoManager;
    private ToolWindow twImages;
    private ToolWindow twSnippets;
    private ToolWindow twTOC;
    private final TableOfContentsView tocView;
    private final SnippetManagerPanel snippetsView;

    @Override
    public SpellChecker getSpellChecker() {

        if (prefs.isSpellCheckingEnabled()) {
            return spellChecker;
        } else {
            return null;
        }
    }

    /**
     * Creates new form MainWindow
     *
     * @param prefs Application preferences
     */
    public MainWindow(final DocumentorPreferences prefs) {
        this.prefs = prefs;
        LongOperation.setFrame(this);

        initComponents();

        setLocation(prefs.getMainWindowX(), prefs.getMainWindowY());
        setSize(prefs.getMainWindowWidth(), prefs.getMainWindowHeight());

        addComponentListener(
                new ComponentListener() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        prefs.setMainWindowWidth(getWidth());
                        prefs.setMainWindowHeight(getHeight());
                    }

                    @Override
                    public void componentMoved(ComponentEvent e) {
                        prefs.setMainWindowX(getX());
                        prefs.setMainWindowY(getY());
                    }

                    @Override
                    public void componentShown(ComponentEvent e) {
                    }

                    @Override
                    public void componentHidden(ComponentEvent e) {
                    }
                });

        try {
            SpellDictionary dictionary = new SpellDictionaryHashMap(
                    new BufferedReader(
                            new InputStreamReader(WikiMarkupEditor.class.getResourceAsStream("/dict/en.txt"))));
            spellChecker = new SpellChecker(dictionary);
        } catch (Exception ex) {
            spellChecker = null;
            ErrorDialog.show(this, "Failed to initialize spell checker", ex);
        }

        spellCheckingMenuItem.setSelected(prefs.isSpellCheckingEnabled());

        doc = prefs.getInjector().getInstance(Documentation.class);

        rebuildExportMenu();

        toolWindowManager = new MyDoggyToolWindowManager();

        ContentManager contentManager = toolWindowManager.getContentManager();
        MyDoggyTabbedContentManagerUI contentManagerUI = new MyDoggyTabbedContentManagerUI();
        contentManager.setContentManagerUI(contentManagerUI);

        contentManagerUI.setShowAlwaysTab(true);
        contentManager.addContentManagerListener(this);

        add(toolWindowManager, BorderLayout.CENTER);

        showPreferencesIfNecessary();

        final StartupDialog startup = new StartupDialog(this, prefs);

        if (prefs.getInitialRoot() == null) {
            startup.setVisible(true);
        }

        boolean loaded = false;
        if (startup.getFinalAction() != StartupDialog.Action.Cancel) {

            if (startup.initialize(doc)) {
                tocView = new TableOfContentsView(doc, this, prefs);
                twTOC = toolWindowManager.registerToolWindow(
                        "TOC", "Table of contents", null,
                        tocView,
                        ToolWindowAnchor.LEFT);

                twTOC.setType(ToolWindowType.DOCKED);
                twTOC.setAutoHide(false);
                twTOC.setVisible(true);
                twTOC.setAvailable(true);
                twTOC.addPropertyChangeListener("visible",
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent pce) {
                                tocItem.setState((Boolean) pce.getNewValue());
                            }
                        });

                twImages = toolWindowManager.registerToolWindow(
                        "IMG",
                        "Image manager",
                        null,
                        new ImageManagerPanel(doc.getImages()),
                        ToolWindowAnchor.LEFT);
                twImages.setType(ToolWindowType.DOCKED);
                twImages.setAutoHide(false);
                twImages.setVisible(true);
                twImages.setAvailable(true);
                twImages.addPropertyChangeListener("visible",
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent pce) {
                                imageManagerItem.setState((Boolean) pce.getNewValue());
                            }
                        });

                snippetsView = new SnippetManagerPanel(this, doc, prefs.getConditions());
                twSnippets = toolWindowManager.registerToolWindow(
                        "SNIP",
                        "Snippets manager",
                        null,
                        snippetsView,
                        ToolWindowAnchor.RIGHT);
                twSnippets.setType(ToolWindowType.DOCKED);
                twSnippets.setAutoHide(false);
                twSnippets.setVisible(true);
                twSnippets.setAvailable(true);
                twSnippets.addPropertyChangeListener("visible",
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent pce) {
                                snippetManagerItem.setState((Boolean) pce.getNewValue());
                            }
                        });

                labelRoot.setText(doc.getRepositoryRoot());

                floatingPreview = new FloatingPreviewWindow(new File(doc.getRepositoryRoot()), this, prefs);
                openOrFocusPage("start", "");

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
            } else {
                saveTimer = statusCheckTimer = removeOrphanedPagesTimer = null;
                floatingPreview = null;
                tocView = null;
                snippetsView = null;
            }
        } else {
            saveTimer = statusCheckTimer = removeOrphanedPagesTimer = null;
            floatingPreview = null;
            tocView = null;
            snippetsView = null;
        }

        if (!loaded) {
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
        openPagemenuItem = new javax.swing.JMenuItem();
        exportMenu = new javax.swing.JMenu();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        preferencesMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        docPreferencesMenuItem = new javax.swing.JMenuItem();
        spellCheckingMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        customizeStylesheetMenuItem = new javax.swing.JMenuItem();
        synchronizeMenu = new javax.swing.JMenu();
        pullMenuItem = new javax.swing.JMenuItem();
        pushMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        tocItem = new javax.swing.JCheckBoxMenuItem();
        imageManagerItem = new javax.swing.JCheckBoxMenuItem();
        snippetManagerItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        refreshViewsItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        enabledConditionsItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        resetLayoutItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        userManualItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Distributed Documentor");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        toolBar.setBackground(new java.awt.Color(255, 255, 204));
        toolBar.setRollover(true);

        jLabel1.setText("Documentation root: ");
        toolBar.add(jLabel1);

        labelRoot.setFont(labelRoot.getFont().deriveFont(labelRoot.getFont().getStyle() | java.awt.Font.BOLD));
        labelRoot.setText("none");
        toolBar.add(labelRoot);
        toolBar.add(filler1);

        labelUncommitted.setBackground(new java.awt.Color(255, 255, 204));
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

        openPagemenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        openPagemenuItem.setText("Open page...");
        openPagemenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openPagemenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openPagemenuItem);

        exportMenu.setText("Export");
        fileMenu.add(exportMenu);
        fileMenu.add(jSeparator4);

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
        editMenu.add(jSeparator2);

        docPreferencesMenuItem.setText("Documentation preferences");
        docPreferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                docPreferencesMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(docPreferencesMenuItem);

        spellCheckingMenuItem.setSelected(true);
        spellCheckingMenuItem.setText("Spell checking");
        spellCheckingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spellCheckingMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(spellCheckingMenuItem);
        editMenu.add(jSeparator6);

        customizeStylesheetMenuItem.setText("Customize stylesheet");
        customizeStylesheetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customizeStylesheetMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(customizeStylesheetMenuItem);

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

        viewMenu.setText("View");

        tocItem.setSelected(true);
        tocItem.setText("Table of contents");
        tocItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tocItemActionPerformed(evt);
            }
        });
        viewMenu.add(tocItem);

        imageManagerItem.setSelected(true);
        imageManagerItem.setText("Image manager");
        imageManagerItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageManagerItemActionPerformed(evt);
            }
        });
        viewMenu.add(imageManagerItem);

        snippetManagerItem.setSelected(true);
        snippetManagerItem.setText("Snippet manager");
        snippetManagerItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snippetManagerItemActionPerformed(evt);
            }
        });
        viewMenu.add(snippetManagerItem);
        viewMenu.add(jSeparator3);

        refreshViewsItem.setText("Refresh all");
        refreshViewsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshViewsItemActionPerformed(evt);
            }
        });
        viewMenu.add(refreshViewsItem);
        viewMenu.add(jSeparator1);

        enabledConditionsItem.setText("Enabled conditions...");
        enabledConditionsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enabledConditionsItemActionPerformed(evt);
            }
        });
        viewMenu.add(enabledConditionsItem);
        viewMenu.add(jSeparator5);

        resetLayoutItem.setText("Reset layout");
        resetLayoutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetLayoutItemActionPerformed(evt);
            }
        });
        viewMenu.add(resetLayoutItem);

        menuBar.add(viewMenu);

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
        LongOperation.shutdown();
    }

    private File getWorkspaceFile() {
        return new File(System.getProperty("user.home"), "documentor.workspace.xml");
    }

    private void saveLayout() {
        PersistenceDelegate delegate = toolWindowManager.getPersistenceDelegate();
        try (FileOutputStream output = new FileOutputStream(getWorkspaceFile())) {
            delegate.save(output);
        } catch (Exception e) {
            log.warn("Failed to save layout: " + e.getMessage());
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

    private void preferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesMenuItemActionPerformed
        showPreferences();
    }//GEN-LAST:event_preferencesMenuItemActionPerformed

    private void pullMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pullMenuItemActionPerformed

        saveTimer.stop();
        statusCheckTimer.stop();
        removeOrphanedPagesTimer.stop();

        try {
            SyncController controller = createSyncConrtoller();
            controller.pull();
        } catch (IOException | FailedToLoadPageException | FailedToLoadTOCException | FailedToLoadMetadataException ex) {
            ErrorDialog.show(this, "Failed to download changes", ex);
        } finally {
            saveTimer.start();
            statusCheckTimer.start();
            removeOrphanedPagesTimer.start();
        }
    }//GEN-LAST:event_pullMenuItemActionPerformed

    private void pushMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pushMenuItemActionPerformed

        saveTimer.stop();
        statusCheckTimer.stop();
        removeOrphanedPagesTimer.stop();

        try {
            SyncController controller = createSyncConrtoller();
            controller.push();
        } catch (IOException | FailedToLoadPageException | FailedToLoadTOCException | FailedToLoadMetadataException ex) {
            ErrorDialog.show(this, "Failed to upload changes", ex);
        } finally {
            saveTimer.start();
            statusCheckTimer.start();
            removeOrphanedPagesTimer.start();
        }
    }//GEN-LAST:event_pushMenuItemActionPerformed

    private void userManualItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userManualItemActionPerformed

        try {
            Desktop.getDesktop().browse(new URI("http://freezingmoon.dyndns.org/"));
        } catch (URISyntaxException | IOException ex) {
            log.error(null, ex);
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

    private void tocItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tocItemActionPerformed

        if (tocItem.getState()) {
            twTOC.setAvailable(true);
            twTOC.setVisible(true);
            twTOC.setActive(true);
        } else {
            twTOC.setVisible(false);
        }
    }//GEN-LAST:event_tocItemActionPerformed

    private void imageManagerItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageManagerItemActionPerformed

        if (imageManagerItem.getState()) {
            twImages.setAvailable(true);
            twImages.setVisible(true);
            twImages.setActive(true);
        } else {
            twImages.setVisible(false);
        }
    }//GEN-LAST:event_imageManagerItemActionPerformed

    private void snippetManagerItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snippetManagerItemActionPerformed

        if (snippetManagerItem.getState()) {
            twSnippets.setAvailable(true);
            twSnippets.setVisible(true);
            twSnippets.setActive(true);
        } else {
            twSnippets.setVisible(false);
        }
    }//GEN-LAST:event_snippetManagerItemActionPerformed

    private void resetLayoutItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetLayoutItemActionPerformed

        twTOC.setAvailable(true);
        twTOC.setVisible(true);
        twTOC.setVisible(true);
        twTOC.setAnchor(ToolWindowAnchor.LEFT);
        twTOC.setType(ToolWindowType.DOCKED);

        twImages.setAvailable(true);
        twImages.setVisible(true);
        twImages.setVisible(true);
        twImages.setAnchor(ToolWindowAnchor.LEFT);
        twImages.setType(ToolWindowType.DOCKED);

        twSnippets.setAvailable(true);
        twSnippets.setVisible(true);
        twSnippets.setVisible(true);
        twSnippets.setAnchor(ToolWindowAnchor.RIGHT);
        twSnippets.setType(ToolWindowType.DOCKED);
    }//GEN-LAST:event_resetLayoutItemActionPerformed

    private void spellCheckingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spellCheckingMenuItemActionPerformed

        prefs.toggleSpellChecking();
        spellCheckingMenuItem.setSelected(prefs.isSpellCheckingEnabled());

    }//GEN-LAST:event_spellCheckingMenuItemActionPerformed

    private void docPreferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_docPreferencesMenuItemActionPerformed

        DocumentationPreferencesDialog dlg = new DocumentationPreferencesDialog(this, doc);
        dlg.setVisible(true);
    }//GEN-LAST:event_docPreferencesMenuItemActionPerformed

    private void refreshViewsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshViewsItemActionPerformed
        refreshAll();
    }//GEN-LAST:event_refreshViewsItemActionPerformed

    private void refreshAll() {
        for (Content content : toolWindowManager.getContentManager().getContents()) {
            if (content.getComponent() instanceof SplittedPageView) {
                ((SplittedPageView) content.getComponent()).refreshPreview();
            }
        }
    }

    private void openPagemenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPagemenuItemActionPerformed

        OpenPageDialog dlg = new OpenPageDialog(this, doc);
        dlg.setVisible(true);
        if (dlg.getReturnStatus() == OpenPageDialog.RET_OK) {
            openOrFocusPage(dlg.getSelectedPageID(), "");
        }

    }//GEN-LAST:event_openPagemenuItemActionPerformed

    private void enabledConditionsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enabledConditionsItemActionPerformed
    
        EnabledConditionsDialog dlg = new EnabledConditionsDialog(this, doc, prefs);
        dlg.setVisible(true);
        refreshAll();
    }//GEN-LAST:event_enabledConditionsItemActionPerformed

    private void customizeStylesheetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customizeStylesheetMenuItemActionPerformed
        
        File customStylesheet = doc.getCustomStylesheet();
        if (!customStylesheet.exists()) {
            try {
                Files.write("/* Customized CSS */\r\n\r\n".getBytes("UTF-8"), customStylesheet);
            } catch (IOException ex) {
                ErrorDialog.show(this, "Failed to create the customized CSS", ex);
            }
        }
        
        openOrFocusStylesheet();        
    }//GEN-LAST:event_customizeStylesheetMenuItemActionPerformed

    private void showPreferencesIfNecessary() {

        if (!prefs.hasValidMercurialPath()) {
            showPreferences();
        }
    }

    private void showPreferences() {
        SettingsDialog dlg = new SettingsDialog(this, true, prefs);
        dlg.setVisible(true);

        for (Content content : toolWindowManager.getContentManager().getContents()) {
            if (content.getComponent() instanceof SplittedPageView) {
                ((SplittedPageView) content.getComponent()).updateFont();
            }
        }

        rebuildExportMenu();
    }

    private void rebuildExportMenu() {
        exportMenu.removeAll();

        ExportMenu exportMenuHandler = prefs.getInjector().getInstance(ExportMenu.class);
        exportMenuHandler.buildMenu(this, exportMenu, doc);
    }

    private void loadLayout() {

        File workspaceFile = new File(System.getProperty("user.home"), "documentor.workspace.xml");

        if (workspaceFile.exists()) {
            PersistenceDelegate delegate = toolWindowManager.getPersistenceDelegate();

            try (FileInputStream input = new FileInputStream(workspaceFile)) {

                delegate.merge(input, PersistenceDelegate.MergePolicy.RESET);

                tocItem.setState(twTOC.isVisible());
                imageManagerItem.setState(twImages.isVisible());
                snippetManagerItem.setState(twSnippets.isVisible());
            } catch (Exception e) {
                log.warn("Failed to load layout: " + e.getMessage());
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            Properties props = new Properties();
            props.load(new FileInputStream("log4j.properties"));
            PropertyConfigurator.configure(props);
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Distributed Documentor");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            log.error(null, ex);
        }

        LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE_WITHOUT_MENU);

        final DocumentorPreferences prefs = new DocumentorPreferences(args);

        if (prefs.exportToCHM() || prefs.exportToHTML()) {

            CommandLineExporter exporter = new CommandLineExporter(prefs);
            exporter.run();
        } else {
            /*
             * Create and display the form
             */
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    java.awt.EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
                    queue.push(new EventQueueProxy());

                    new MainWindow(prefs).setVisible(true);
                }
            });
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCommit;
    private javax.swing.JButton btRevert;
    private javax.swing.JMenuItem customizeStylesheetMenuItem;
    private javax.swing.JMenuItem docPreferencesMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem enabledConditionsItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu exportMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JCheckBoxMenuItem imageManagerItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JLabel labelRoot;
    private javax.swing.JLabel labelUncommitted;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openPagemenuItem;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JMenuItem pullMenuItem;
    private javax.swing.JMenuItem pushMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem refreshViewsItem;
    private javax.swing.JMenuItem resetLayoutItem;
    private javax.swing.JCheckBoxMenuItem snippetManagerItem;
    private javax.swing.JCheckBoxMenuItem spellCheckingMenuItem;
    private javax.swing.JMenu synchronizeMenu;
    private javax.swing.JCheckBoxMenuItem tocItem;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenuItem userManualItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

    @Override
    public void openOrFocusPage(final String id, final String anchor) {

        ContentManager contentManager = toolWindowManager.getContentManager();

        Content content = contentManager.getContent(id);
        Page page = doc.getPage(id);
        if (content == null) {
            content = contentManager.addContent(
                    id,
                    "Page: " + id,
                    null,
                    new SplittedPageView(page, new File(doc.getRepositoryRoot()), this, prefs));
        }

        content.setSelected(true);
        floatingPreview.switchPage(page);

        addSyncTOCItem(content, page);

        if (!anchor.isEmpty()) {

            final SplittedPageView pageView = ((SplittedPageView) content.getComponent());

            EventQueue.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            pageView.scrollToId(anchor);
                        }
                    });
        }
    }

    @Override
    public void openOrFocusSnippet(String id) {

        ContentManager contentManager = toolWindowManager.getContentManager();
        String contentId = "Snippet:" + id;
        Snippet snippet = doc.getSnippet(id);

        Content content = contentManager.getContent(contentId);
        if (content == null) {
            content = contentManager.addContent(
                    contentId,
                    "Snippet: " + id,
                    null,
                    new SplittedPageView(snippet, new File(doc.getRepositoryRoot()), this, prefs));
        }

        content.setSelected(true);

        addSyncTOCItem(content, snippet);

        floatingPreview.switchPage(snippet);
    }

    private void onSaveTimerTick() {
        try {
            doc.saveAll();
        } catch (CouldNotSaveDocumentationException ex) {
            log.error("Failed to save documentation: ", ex);
        }
    }

    private void onStatusCheckTimerTick() {

        boolean hasChanges = doc.getVersionControl().hasChanges();

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

        if (cme.getContent().getComponent() instanceof SplittedPageView) {
            SplittedPageView view = (SplittedPageView) cme.getContent().getComponent();
            view.dispose();
        }
    }

    @Override
    public void contentSelected(ContentManagerEvent cme) {

        Component comp = cme.getContent().getComponent();
        if (comp instanceof SplittedPageView) {
            SplittedPageView view = (SplittedPageView) comp;

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
        // TODO: get sync controller from the DI container
        MercurialSync hg = new MercurialSync(doc, prefs, LongOperation.get());
        DialogBasedSyncInteraction dlgui = new DialogBasedSyncInteraction(this, doc);
        SyncController controller = new SyncController(hg, hg, hg, dlgui, doc, this);
        return controller;
    }

    @Override
    public FloatingPreview getFloatingPreview() {
        return floatingPreview;
    }

    private void addSyncTOCItem(final Content content, final Page page) {
        JPopupMenu popup = new JPopupMenu();

        if (content.getContentUI().isCloseable()) {
            popup.add(new JMenuItem(new AbstractAction(SwingUtil.getString("@@tabbed.page.close")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toolWindowManager.getContentManager().removeContent(content);
                }
            }));
        }

        popup.add(new JMenuItem(new AbstractAction(SwingUtil.getString("@@tabbed.page.closeAll")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                toolWindowManager.getContentManager().removeAllContents();
            }
        }));

        popup.add(new JMenuItem(new AbstractAction(SwingUtil.getString("@@tabbed.page.closeAllButThis")) {
            @Override
            public void actionPerformed(ActionEvent e) {

                List<Content> toRemove = new LinkedList<>();
                for (Content otherContent : toolWindowManager.getContentManager().getContents()) {
                    if (content != otherContent && otherContent.getContentUI().isCloseable()) {
                        toRemove.add(otherContent);
                    }
                }

                for (Content c : toRemove) {
                    toolWindowManager.getContentManager().removeContent(c);
                }
            }
        }));

        String targetName;
        if (page instanceof Snippet) {
            targetName = "snippet list";
        } else {
            targetName = "TOC";
        }

        JMenuItem item = new JMenuItem("Sync in " + targetName);
        popup.add(new JSeparator());
        popup.add(item);

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (page instanceof Snippet) {
                    snippetsView.selectSnippet((Snippet) page);
                } else {
                    tocView.selectPage(page);
                }
            }
        });

        content.setPopupMenu(popup);
    }

    @Override
    public void openOrFocusStylesheet() {
        ContentManager contentManager = toolWindowManager.getContentManager();
        String contentId = "custom.css";

        Content content = contentManager.getContent(contentId);
        if (content == null) {
            content = contentManager.addContent(
                    contentId,
                    "Custom stylesheet",
                    null,
                    new CSSView(doc, prefs));
        }

        content.setSelected(true);
    }
}
