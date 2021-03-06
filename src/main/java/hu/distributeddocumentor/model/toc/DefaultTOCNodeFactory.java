package hu.distributeddocumentor.model.toc;

import hu.distributeddocumentor.model.Page;
import java.io.File;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class DefaultTOCNodeFactory implements TOCNodeFactory {   
    private final File customStylesheet;
    
    public DefaultTOCNodeFactory(File customStylesheet) {
        this.customStylesheet = customStylesheet;                
    }

    @Override
    public TOCNode fromXML(Node node) {
        Node sibling;
        for (sibling = node; sibling != null; sibling = sibling.getNextSibling()) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                break;
            }
        }
        
        if (sibling != null) {
            Element elem = (Element)sibling;
            
            if (elem.hasAttribute("virtual-hierarchy-builder")) {
                return new DefaultVirtualTOCNode(this, customStylesheet);
            }
        }
        
        return new DefaultTOCNode(this);
    }

    @Override
    public TOCNode createNode(String title) {
        return new DefaultTOCNode(this, title);
    }

    @Override
    public TOCNode createNode(Page target) {
        return new DefaultTOCNode(this, target);
    }

    @Override
    public VirtualTOCNode createVirtualNode() {
        return new DefaultVirtualTOCNode(this, customStylesheet);
    }

    @Override
    public TOCNodeOperations getOperations(TOCNode node) {
        return (DefaultTOCNode)node;
    }

    @Override
    public TOCNodeSerialization getSerialization(TOCNode node) {
        return (DefaultTOCNode)node;
    }

}
