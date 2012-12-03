package hu.distributeddocumentor.model.toc;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class DefaultTOCNodeFactory implements TOCNodeFactory {

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
                return new VirtualTOCNode();
            }
        }
        
        return new TOCNode();
    }

}
