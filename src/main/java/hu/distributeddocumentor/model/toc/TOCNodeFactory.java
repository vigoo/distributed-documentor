package hu.distributeddocumentor.model.toc;

import org.w3c.dom.Node;

/**
 *
 * @author Daniel Vigovszky
 */
public interface TOCNodeFactory {
     TOCNode fromXML(Node node);
}
