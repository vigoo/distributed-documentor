package hu.distributeddocumentor.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * This class was copied and modified from the RSyntaxTextArea project.
 * @author Robert Futrell, modified by Daniel Vigovszky
 */
public class SquiggleUnderlineHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

    private static final Logger log = LoggerFactory.getLogger(SquiggleUnderlineHighlightPainter.class.getName());
    private static final int AMT = 2;

    /**
     * Constructor.
     *
     * @param color The color of the squiggle. This cannot be <code>null</code>.
     */
    public SquiggleUnderlineHighlightPainter(Color color) {
        super(color);        
    }

    /**
     * Paints a portion of a highlight.
     *
     * @param g the graphics context
     * @param offs0 the starting model offset >= 0
     * @param offs1 the ending model offset >= offs1
     * @param bounds the bounding box of the view, which is not necessarily the
     * region to paint.
     * @param c the editor
     * @param view View painting for
     * @return region drawing occurred in
     */ 
    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1,
            Shape bounds, JTextComponent c, View view) {

        g.setColor(getColor());

        if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
            // Contained in view, can just use bounds.
            Rectangle alloc;
            if (bounds instanceof Rectangle) {
                alloc = (Rectangle) bounds;
            } else {
                alloc = bounds.getBounds();
            }
            paintSquiggle(g, alloc);
            return alloc;
        }

        // Otherwise, should only render part of View.
        try {
            // --- determine locations ---
            Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                    offs1, Position.Bias.Backward,
                    bounds);
            Rectangle r = (shape instanceof Rectangle)
                    ? (Rectangle) shape : shape.getBounds();
            paintSquiggle(g, r);
            return r;
        } catch (BadLocationException e) {
            log.error("Cannot render highlight: " + e.getMessage());
        }

        // Only if exception
        return null;

    }

    /**
     * Paints a squiggle underneath text in the specified rectangle.
     *
     * @param g The graphics context with which to paint.
     * @param r The rectangle containing the text.
     */
    protected void paintSquiggle(Graphics g, Rectangle r) {
        int x = r.x;
        int y = r.y + r.height - 1;
        int delta = -AMT;
        while (x < r.x + r.width) {
            g.drawLine(x, y, x + AMT, y + delta);
            y += delta;
            delta = -delta;
            x += AMT;
        }
    }
}