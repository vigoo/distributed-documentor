package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import hu.distributeddocumentor.utils.XmlUtils;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


public class MethodDoc extends DocItem {

    private static final Logger log = LoggerFactory.getLogger(MethodDoc.class);
    
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
        
        //log.debug("Generating documentation for method " + name + " from the following element:");
        //XmlUtils.dumpElement(log, elem);
        
        Element returnsElem = XmlUtils.getSingleElement(elem, "reflection/returns");
        
        if (returnsElem != null) {
            renderType(returnsElem, code, getParent().getParent().getAsPrefix());            
        } else {
            code.append("?");
        }
        
        code.append(" ");
        code.append(name);
        code.append("(");
        
        Element[] parameters = XmlUtils.getElements(elem, "reflection/parameters/parameter");
        
        for (int i = 0; i < parameters.length; i++) {

            if (i > 0) {
                code.append(", ");
            }                

            Element param = parameters[i];

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
        
        code.append(")");
        
        writer.sourceCode("csharp", code.toString());
        writer.newParagraph();

        Element summaryElem = XmlUtils.getSingleElement(elem, "summary");
        if (summaryElem != null) {                
            renderDocElem(writer, summaryElem);
        }

        Element[] paramDocElems = XmlUtils.getElements(elem, "param");
        if (paramDocElems.length > 0) {
            
            writer.text("The parameters have the following meaning:");
            writer.newParagraph();
            for (Element paramDoc : paramDocElems) {
                writer.beginBullet(1);
                writer.bold(paramDoc.getAttribute("name"));
                writer.text(": ");
                renderDocElem(writer, paramDoc);
                writer.text("\n");
            }        
        }
        
        renderContract(writer, elem);

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
