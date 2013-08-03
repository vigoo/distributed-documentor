package hu.distributeddocumentor.gui;

import hu.distributeddocumentor.gui.htmlpreview.SVGSalamanderReplacedElementFactory;
import hu.distributeddocumentor.model.Page;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Element;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;

public final class HTMLPreview extends javax.swing.JPanel implements Observer, PreviewSync {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HTMLPreview.class.getName());

    private final File root;
    private final FSScrollPane scrollPane;
    private final XHTMLPanel htmlPanel;
    private final NaiveUserAgent uac;
    private Page page;    
    
    /**
     * Creates new form HTMLPreview
     */
    public HTMLPreview(Page page, final PageEditorHost host, File root) {
        initComponents();
                
        this.root = root;                
        
        htmlPanel = new XHTMLPanel();
        htmlPanel.setVisible(true);
        
        scrollPane = new FSScrollPane(htmlPanel);
                
        add(scrollPane, BorderLayout.CENTER);

        final String rootUri = root.toURI().toString();        
        
        NaiveUserAgent existingUac = (NaiveUserAgent) htmlPanel.getSharedContext().getUserAgentCallback();
        if (existingUac == null) {
            uac = new NaiveUserAgent();

            uac.setBaseURL(rootUri);

            htmlPanel.getSharedContext().setUserAgentCallback(uac);        
        } else {
            uac = existingUac;
        }
        
        htmlPanel.getSharedContext().setReplacedElementFactory(
                new SVGSalamanderReplacedElementFactory(
                    new SwingReplacedElementFactory()));
                       
        for (Object listener : htmlPanel.getMouseTrackingListeners()) {
            if (listener instanceof LinkListener) {
                htmlPanel.removeMouseTrackingListener((FSMouseListener)listener);
            }
        }
        
        htmlPanel.addMouseTrackingListener(
                new LinkListener() {

                    @Override
                    public void linkClicked(BasicPanel panel, String uri) {
                        
                        if (!uri.startsWith("http://") && 
                            !uri.startsWith("https://") &&
                            !uri.startsWith("file://") &&
                             uri.length() > ".html".length()) {
                            
                            String[] parts = uri.split("#");
                            String id = parts[0].substring(0, parts[0].length() - ".html".length());
                            String anchor = parts.length > 1 ? parts[1] : "";
                                                    
                            host.openOrFocusPage(id, anchor);
                            
                        } else if (uri.toString().startsWith(rootUri)) {
                            String fileName = uri.toString().substring(rootUri.length());
                            fileName = fileName.substring(0, fileName.length() - ".html".length());

                            host.openOrFocusPage(fileName, "");
                        } else {
                            try {
                                Desktop.getDesktop().browse(new URI(uri));
                            }
                            catch (URISyntaxException | IOException ex) {
                                log.error(null, ex);
                            }
                        }                        
                    }                    
                });
        
        switchPage(page);
    }

    /**
     * Refresh the current preview
     */
    public void refresh() {
            
        uac.clearImageCache();
        renderPage();
    }
    
    public void switchPage(Page newPage) {
        if (page != newPage) {
            if (page != null) {
                page.deleteObserver(this);
            }

            page = newPage;

            if (page != null) {
                page.addObserver(this);
                renderPage();
            }
        }
    }

    @Override
    public void scrollToLine(int lineIdx) {
        
        String lineId = "line"+Integer.toString(lineIdx);
        
        scrollToId(lineId);
    }
    
    @Override
    public void scrollToId(String id) {
       
        log.debug("Trying to scroll to " + id);
        
        Box rootBox = htmlPanel.getRootBox();
        if (rootBox != null) {
            Box lineAnnotation = findId(rootBox, id);

            if (lineAnnotation != null) {

                Point pt = new Point(lineAnnotation.getAbsX(), lineAnnotation.getAbsY());
                int top = scrollPane.getVerticalScrollBar().getValue();
                int bottom = top + scrollPane.getHeight();

                if (pt.y < top || pt.y > bottom) {
                    htmlPanel.scrollTo(pt);
                }
            }
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

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        renderPage();
    }//GEN-LAST:event_formComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    private void renderPage() {
        // Getting the HTML representation of the page
        String html = page.asAnnotatedHTMLembeddingCSS();
        byte[] htmlBytes = html.getBytes(Charset.forName("utf-8"));
        
        try {
            log.debug("Setting HTML renderer's document");
            htmlPanel.setDocument(new ByteArrayInputStream(htmlBytes), root.toURI().toString());            
            
        } catch (Exception ex) {
            log.error(null, ex);            
            
            String simpleHtml = page.asHTMLembeddingCSS();
            htmlBytes = simpleHtml.getBytes(Charset.forName("utf-8"));
        
            try {
                htmlPanel.setDocument(new ByteArrayInputStream(htmlBytes), root.toURI().toString());            

            } catch (Exception iex) {
            
                String errorHtml = "<?xml version='1.0' encoding='utf-8'?><html xmlns='http://www.w3.org/1999/xhtml'><body><h1>Failed to render page</h1><pre>"+
                                StringEscapeUtils.escapeXml(iex.toString())+
                                "</pre></body></html>";
            
                htmlBytes = errorHtml.getBytes(Charset.forName("utf-8"));
        
                try {
                    htmlPanel.setDocument(new ByteArrayInputStream(htmlBytes), root.toURI().toString());            

                } catch (Exception iiex) {
                    log.error(null, iiex);            
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object o1) {
        renderPage();
    }
    
    private Box findId(Box box, String id) {
        
        Element elem = box.getElement();
        if (elem != null) {
            if (elem.hasAttribute("id")) {                
                // We are using 'endsWith' here because the adding the line number 
                // spans to header elements corrupts their id which is otherwise used
                // as anchors. This way the method can work with both the line number spans
                // and the mediawiki style automatic anchors.
                if (elem.getAttribute("id").endsWith(id)) {
                    return box;
                }                                    
            }
        }
        
        for (Iterator it = box.getChildIterator(); it.hasNext();) {
            Box child = (Box) it.next();
            Box result = findId(child, id);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }

   
}
