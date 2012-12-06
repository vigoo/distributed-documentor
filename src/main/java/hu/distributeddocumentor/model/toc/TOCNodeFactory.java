package hu.distributeddocumentor.model.toc;

import hu.distributeddocumentor.model.Page;
import org.w3c.dom.Node;

/**
 *
 * @author Daniel Vigovszky
 */
public interface TOCNodeFactory {
    
    TOCNode createNode(String title);
    TOCNode createNode(Page target);    
    VirtualTOCNode createVirtualNode();
    
    TOCNode fromXML(Node node);
}
