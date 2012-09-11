package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import hu.distributeddocumentor.utils.XmlUtils;
import java.io.IOException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class MethodDoc extends DocItem {

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
    
    public MethodDoc(ClassDoc parent, String name, Function<String, String> idGenerator) {
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
                        
        Element elem = getElem();
        Element returnsElem = XmlUtils.getSingleElement(elem, "reflection/returns");
        
        if (returnsElem != null) {
            renderType(returnsElem, code, getParent().getParent().getAsPrefix());            
        } else {
            code.append("?");
        }
        
        code.append(" ");
        code.append(name);
        code.append("(");
        
        NodeList parameters = XmlUtils.getElements(elem, "reflection/parameters/parameter");
        if (parameters != null) {
            for (int i = 0; i < parameters.getLength(); i++) {
                
                if (i > 0) {
                    code.append(", ");
                }                
                
                Element param = (Element)parameters.item(i);
                
                if (XmlUtils.isAttributeTrue(param, "is-in")) {
                    code.append("in ");
                }
                if (XmlUtils.isAttributeTrue(param, "is-out")) {
                    code.append("out ");
                }
                
                renderType(param, code, getParent().getParent().getAsPrefix());
                code.append(" ");        
                code.append(param.getAttribute("name"));
            }
        }
        
        code.append(")");
        
        writer.sourceCode("csharp", code.toString());
        writer.newParagraph();

        Element summaryElem = XmlUtils.getSingleElement(elem, "summary");
        if (summaryElem != null) {                
            renderDocElem(writer, summaryElem);
        }

        // TODO: parameter doc
        
        renderContract(writer, elem);

        Element remarksElem = XmlUtils.getSingleElement(elem, "remarks");
        if (remarksElem != null) {
            writer.heading(headingLevel + 1, "Remarks");
            renderDocElem(writer, remarksElem);
        }

        NodeList seeAlsoElems = XmlUtils.getElements(elem, "seealso");
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
