package hu.distributeddocumentor.gui;

import java.awt.Frame;

public interface PageEditorHost {
    void openOrFocusPage(String id);
    void openOrFocusSnippet(String id);
    
    void documentationReloaded();
    void updateUndoRedoItems();

    public Frame getMainFrame();   
}
