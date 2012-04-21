package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.gui.PageEditorHost;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.w3c.dom.html2.HTMLElement;


public class HtmlPreviewRendererContext extends SimpleHtmlRendererContext {

    private URI rootURI;
    private PageEditorHost host;
    
    public HtmlPreviewRendererContext(HtmlPanel contextComponent, UserAgentContext ucontext, URI rootURI, PageEditorHost host) {
        super(contextComponent, ucontext);
        
        this.rootURI = rootURI;
        this.host = host;
    }

    @Override
    public void linkClicked(HTMLElement linkNode, URL url, String target) {
        
        try {
            URI uri = url.toURI();
                
            if (uri.toString().startsWith(rootURI.toString())) {
                
                String fileName = uri.toString().substring(rootURI.toString().length());
                fileName = fileName.substring(0, fileName.length() - ".html".length());
                
                host.openOrFocusPage(fileName);
            
            } else {
                Desktop.getDesktop().browse(uri);
            }
        } catch (Exception ex) {
                Logger.getLogger(HtmlPreviewRendererContext.class.getName()).log(Level.SEVERE, null, ex);
        }                           
    }

    
    
}
