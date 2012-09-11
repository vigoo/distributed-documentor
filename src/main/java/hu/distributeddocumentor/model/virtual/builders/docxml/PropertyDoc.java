package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import hu.distributeddocumentor.utils.XmlUtils;
import java.io.IOException;
import org.w3c.dom.Element;


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
        Element reflectionElem = XmlUtils.getSingleElement(getElem(), "reflection");
        if (reflectionElem != null) {
            propertyElem = XmlUtils.getSingleElement(reflectionElem, "property");
        }
        
        if (propertyElem != null) {
            renderType(propertyElem, code, getParent().getParent().getAsPrefix());            
        } else {
            code.append("?");
        }
        
        code.append(" ");
        code.append(name);
        code.append(" { ");
        
        if (propertyElem != null) {
            if (XmlUtils.isAttributeTrue(propertyElem, "can-read")) {                
                code.append("get; ");
            }
            
            if (XmlUtils.isAttributeTrue(propertyElem, "can-write")) {
                code.append("set; ");
            }
        }
        
        code.append("}");
        
        writer.sourceCode("csharp", code.toString());
        writer.newParagraph();
        
        Element elem = getElem();
        if (elem != null) {

            Element summaryElem = XmlUtils.getSingleElement(elem, "summary");
            if (summaryElem != null) {                
                renderDocElem(writer, summaryElem);
            }
            
            Element getterElem = XmlUtils.getSingleElement(elem, "getter");
            if (getterElem != null) {
                writer.heading(headingLevel + 1, "Getter contract");
                renderContract(writer, getterElem);
            }
            
            Element setterElem = XmlUtils.getSingleElement(elem, "setter");
            if (setterElem != null) {
                writer.heading(headingLevel + 1, "Setter contract");
                renderContract(writer, setterElem);
            }

            Element remarksElem = XmlUtils.getSingleElement(elem, "remarks");
            if (remarksElem != null) {
                writer.heading(headingLevel + 1, "Remarks");
                renderDocElem(writer, remarksElem);
            }

            Element[] seeAlsoElems = XmlUtils.getElements(elem, "seealso");
            if (seeAlsoElems.length > 0) {
                writer.heading(headingLevel + 1, "See also");

                for (Element seeAlsoElem : seeAlsoElems) {
                    
                    writer.beginBullet(1);
                    writeReference(writer, seeAlsoElem.getAttribute("cref"));
                    writer.text("\n");
                }

                writer.newParagraph();
            }
        }
    }
}
