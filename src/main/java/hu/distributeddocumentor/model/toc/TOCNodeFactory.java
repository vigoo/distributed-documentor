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
        
    /**
     * Gets the interface for TOC operations, to be used by the TOC class
     * @return TOC node operations interface
     */
    TOCNodeOperations getOperations(TOCNode node);
    
    /**
     * Gets the serialization interface for the TOC node
     * @return TOC node serialization interface
     */
    TOCNodeSerialization getSerialization(TOCNode node);
}
