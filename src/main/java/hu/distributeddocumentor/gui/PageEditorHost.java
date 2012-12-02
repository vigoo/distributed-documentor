package hu.distributeddocumentor.gui;

import com.swabunga.spell.event.SpellChecker;
import java.awt.Frame;

public interface PageEditorHost {
    void openOrFocusPage(String id, String anchor);
    void openOrFocusSnippet(String id);
    
    void documentationReloaded();
    void updateUndoRedoItems();

    public Frame getMainFrame();

    public SpellChecker getSpellChecker();
    
    public FloatingPreview getFloatingPreview();
}
