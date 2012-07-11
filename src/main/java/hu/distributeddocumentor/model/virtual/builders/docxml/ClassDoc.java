package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class ClassDoc extends DocItem {
    
    private final static Logger log = LoggerFactory.getLogger(ClassDoc.class);

    private final String name;
    private final NamespaceDoc parent;
    private final String pageId;        

    public String getName() {
        return name;
    }
    
    public String getFullName() {
        return getParent().getAsPrefix()+getName();
    }

    public NamespaceDoc getParent() {
        return parent;
    }

    public String getPageId() {
        return pageId;
    }        
    
    public ClassDoc(NamespaceDoc parent, String name, String pageId, Function<String, String> idGenerator) {
        super(idGenerator);
        
        this.name = name;
        this.parent = parent;
        this.pageId = pageId;
    }        
        
    public void writeBullet(WikiWriter writer, int level) throws IOException {
        
        writer.beginBullet(level);
        writer.internalLink(pageId, name);
        writer.text("\n");                
    }
    
    public void renderPage(WikiWriter writer) {
                   
        try {
            writer.heading(1, getFullName());
            writer.newParagraph();

            writer.internalLink(getParent().getPageId(), "Parent namespace: " + getParent().getFullName());
            writer.newParagraph();

            Element elem = getElem();
            if (elem != null) {

                Element summaryElem = getFirstElementByName(elem, "summary");
                if (summaryElem != null) {
                    writer.heading(2, "Summary");
                    renderDocElem(writer, summaryElem);
                }

                Element remarksElem = getFirstElementByName(elem, "remarks");
                if (remarksElem != null) {
                    writer.heading(2, "Remarks");
                    renderDocElem(writer, remarksElem);
                }

                NodeList seeAlsoElems = elem.getElementsByTagName("seealso");
                if (seeAlsoElems.getLength() > 0) {
                    writer.heading(2, "See also");

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
        catch (IOException ex) {
            log.error("Failed to render class page: " + getName() + " because of: " + ex.getMessage());
            // TODO: write exception to log and generated page
        }            
    }

}
