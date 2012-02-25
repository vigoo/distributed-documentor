/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.distributeddocumentor.gui;

import hu.distributeddocumentor.model.Page;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.demo.DOMSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author vigoo
 */
public class HTMLPreview extends javax.swing.JPanel implements Observer {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HTMLPreview.class.getName());
    private final Page page;
    private final File root;
    
    
    /**
     * Creates new form HTMLPreview
     */
    public HTMLPreview(Page page, File root) {
        initComponents();
        
        this.page = page;
        this.root = root;
        
        page.addObserver(this);
        
        renderPage();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        renderPage();
    }//GEN-LAST:event_formComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables


    private void renderPage() {
        try {            
            // Getting the HTML representation of the page
            String html = page.asHTML();

            // Parsing the page     
            DOMSource domSoure = new DOMSource(new ByteArrayInputStream(html.getBytes("UTF-8")));
            Document doc = domSoure.parse();
            
            // Analyzing DOM
            DOMAnalyzer da = new DOMAnalyzer(doc);
            da.attributesToStyles();
            da.addStyleSheet(null, CSSNorm.stdStyleSheet());
            da.addStyleSheet(null, CSSNorm.userStyleSheet());
            da.getStyleSheets();
           // da.printTagTree(System.out);
            
            // Creating the browser canvas
            Dimension size = scrollPane.getSize();
            if (size.width == 0 && size.height == 0)
                size = new Dimension(1, 1);
            
            BrowserCanvas browser = new BrowserCanvas(da.getRoot(), da, size, root.toURL());
            
            int vertical = scrollPane.getVerticalScrollBar().getValue();
            scrollPane.setViewportView(browser);                        
            browser.setVisible(true);
            
            vertical = Math.min(scrollPane.getVerticalScrollBar().getMaximum(), vertical);
            scrollPane.getVerticalScrollBar().setValue(vertical);
            
            //System.out.println(html);            
            
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void update(Observable o, Object o1) {
        renderPage();
    }                    
}
