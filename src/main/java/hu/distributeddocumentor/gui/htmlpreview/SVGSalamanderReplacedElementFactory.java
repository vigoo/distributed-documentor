package hu.distributeddocumentor.gui.htmlpreview;

import com.kitfox.svg.app.beans.SVGPanel;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.RootPanel;
import org.xhtmlrenderer.swing.SwingReplacedElement;

/**
 * Factory to replace image elements referring to SVG files to SVGPanel controls
 * @author Daniel Vigovszky
 */
public class SVGSalamanderReplacedElementFactory implements ReplacedElementFactory {

    private final ReplacedElementFactory baseFactory;
    
    public SVGSalamanderReplacedElementFactory(ReplacedElementFactory baseFactory) {
        this.baseFactory = baseFactory;                
    }
    
    @Override
    public ReplacedElement createReplacedElement(LayoutContext context, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
        
        Element e = box.getElement();
        
        if (e != null) {
            
            if (context.getNamespaceHandler().isImageElement(e)) {
                
                String imageSrc = context.getNamespaceHandler().getImageSourceURI(e);
                String ruri = uac.resolveURI(imageSrc);
                
                if (ruri.toLowerCase().endsWith(".svg")) {
                    
                    try {
                        return createSVGPanel(context, new URI(ruri), cssWidth, cssHeight);
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(SVGSalamanderReplacedElementFactory.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                    }
                }
            }
        }
        
        return baseFactory.createReplacedElement(context, box, uac, cssWidth, cssWidth);
    }

    @Override
    public void reset() {
        baseFactory.reset();
    }

    @Override
    public void remove(Element elmnt) {
        baseFactory.reset();
    }

    @Override
    public void setFormSubmissionListener(FormSubmissionListener fl) {
        baseFactory.setFormSubmissionListener(fl);
    }

    private ReplacedElement createSVGPanel(LayoutContext context, URI uri, int cssWidth, int cssHeight) {
        
        SVGPanel panel = new SVGPanel();
        panel.setAntiAlias(true);                
        panel.setSvgURI(uri);
        
        int width = cssWidth > 0 ? cssWidth : panel.getSVGWidth();
        int height = cssHeight > 0 ? cssHeight : panel.getSVGHeight();
                
        panel.setScaleToFit(true);        
        panel.setPreferredSize(new Dimension(width, height));
        panel.setSize(panel.getPreferredSize());
        
        SwingReplacedElement result = new SwingReplacedElement(panel);
        if (context.isInteractive()) {
            RootPanel canvas = context.getCanvas();
            canvas.add(panel);
        }
        
        return result;
    }

}
