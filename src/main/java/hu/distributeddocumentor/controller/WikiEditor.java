
package hu.distributeddocumentor.controller;

import java.awt.Color;

public interface WikiEditor {
    public void setHeadingLevel(final int line, final int level);
    public void setBold(final int start, final int end);
    public void setItalic(final int start, final int end);
    public void setColor(final int start, final int end, final Color color);
    public void toggleBullets(int startRow, int endRow);
    public void toggleEnumeration(int startRow, int endRow);
    public void indent(int startRow, int endRow);
    public void unindent(int startRow, int endRow);
    public void insertRemoteLink(int pos, String url, String linkText);
}
