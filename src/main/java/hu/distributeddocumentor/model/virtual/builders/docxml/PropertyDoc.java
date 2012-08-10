package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class PropertyDoc extends DocItem {

    private final String name;
    private final ClassDoc parent;

    public String getName() {
        return name;
    }
    
    public String getFullName() {
        return getParent().getFullName()+"."+getName();
    }

    public ClassDoc getParent() {
        return parent;
    }
    
    public PropertyDoc(ClassDoc parent, String name, Function<String, String> idGenerator) {
        super(idGenerator);
        
        this.name = name;
        this.parent = parent;        
    }

    public void writeBullet(WikiWriter writer, int level) throws IOException {
        writer.beginBullet(level);
        writer.internalLink("#"+name, name);
        writer.text("\n");                
    }

    public void renderDetails(WikiWriter writer, int headingLevel) throws IOException {
        
        writer.heading(headingLevel, name);
        writer.newParagraph();
        
        StringBuilder code = new StringBuilder();
        
        Element propertyElem = null;
        Element reflectionElem = getFirstElementByName(getElem(), "reflection");
        if (reflectionElem != null)
            propertyElem = getFirstElementByName(propertyElem, "property");
        
        if (propertyElem != null) {
            renderType(propertyElem, code);            
        } else {
            code.append("?");
        }
        
        code.append(" ");
        code.append(name);
        code.append(" { ");
        
        if (propertyElem != null) {
            if (propertyElem.hasAttribute("can-read") &&
                "true".equals(propertyElem.getAttribute("can-read"))) {
                code.append("get; ");
            }
            
            if (propertyElem.hasAttribute("can-write") &&
                "true".equals(propertyElem.getAttribute("can-write"))) {
                code.append("set; ");
            }
        }
        
        code.append("}");
        
        writer.sourceCode("csharp", code.toString());
        writer.newParagraph();
        
        Element elem = getElem();
        if (elem != null) {

            Element summaryElem = getFirstElementByName(elem, "summary");
            if (summaryElem != null) {                
                renderDocElem(writer, summaryElem);
            }

            Element remarksElem = getFirstElementByName(elem, "remarks");
            if (remarksElem != null) {
                writer.heading(headingLevel + 1, "Remarks");
                renderDocElem(writer, remarksElem);
            }

            NodeList seeAlsoElems = elem.getElementsByTagName("seealso");
            if (seeAlsoElems.getLength() > 0) {
                writer.heading(headingLevel + 1, "See also");

                for (int i = 0; i < seeAlsoElems.getLength(); i++) {
                    Element seeAlsoElem = (Element)seeAlsoElems.item(i);

                    writer.beginBullet(1);
                    writeReference(writer, seeAlsoElem.getAttribute("cref"));
                    writer.text("\n");
                }

                writer.newParagraph();
            }
        }
    }
}
