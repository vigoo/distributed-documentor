package hu.distributeddocumentor.gui;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import hu.distributeddocumentor.model.Page;
import java.awt.BorderLayout;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import org.apache.commons.lang3.StringUtils;

public class NativeBrowserPreview extends javax.swing.JPanel implements Observer {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(NativeBrowserPreview.class.getName());
    
    private final JWebBrowser webBrowser;
    private final Page page;
    private final PageEditorHost host;
    private final File root;

    public NativeBrowserPreview(Page page, final PageEditorHost host, File root) {         
        super(new BorderLayout());
        
        this.page = page;
        this.host = host;
        this.root = root;
        
        webBrowser = new JWebBrowser(JWebBrowser.destroyOnFinalization(), JWebBrowser.proxyComponentHierarchy());
        webBrowser.setBarsVisible(false);
        webBrowser.setStatusBarVisible(false);
        
        webBrowser.addWebBrowserListener(
                new WebBrowserAdapter() {
                    @Override
                    public void locationChanging(WebBrowserNavigationEvent e) {
                        
                        final String location = StringUtils.removeStart(e.getNewResourceLocation(), "about:");
                        if (!"blank".equals(location)) {
                            if (location.endsWith(".html")) {

                                String id = location.substring(0, location.length()-5);                            
                                host.openOrFocusPage(id);
                            }

                            e.consume();
                        }
                        else {
                            super.locationChanging(e);
                        }
                    }                    
                });
        webBrowser.setDefaultPopupMenuRegistered(false);
        
        add(webBrowser, BorderLayout.CENTER);
        
        page.addObserver(this);
        
        renderPage();
    }
    
    private void renderPage() {        
            
        // Getting the HTML representation of the page
        String html = page.asHTMLembeddingCSS(root);
        
        webBrowser.setHTMLContent(html);            
    }    

    @Override
    public void update(Observable o, Object o1) {
        renderPage();
    }

    void dispose() {
        webBrowser.disposeNativePeer(false);        
    }
}
