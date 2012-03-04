package hu.distributeddocumentor.gui;

import java.awt.Frame;

public interface PageEditorHost {
    void openOrFocusPage(String id);
    void documentationReloaded();

    public Frame getMainFrame();
}
