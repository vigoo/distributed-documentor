
package hu.distributeddocumentor.model.toc;

import hu.distributeddocumentor.model.Documentation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface TOCNodeSerialization {

    /**
     * Loads the node from its XML representation
     *
     * @param node the XML node representing this TOC node
     * @param doc the XML document being loaded
     * @param factory the TOCNode class factory
     * @throws ClassNotFoundException if the referenced virtual hierarchy
     * builder class does not exist
     */
    void fromXML(Node node, Documentation doc, TOCNodeFactory factory) throws ClassNotFoundException;

    /**
     * Creates the node's XML representation for serialization
     *
     * @param doc the XML document being constructed
     * @return returns the XML node representing this TOC node
     */
    Node toXML(Document doc);
    
}
