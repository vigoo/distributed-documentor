package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import java.net.URI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ClassDoc {

    private final String name;
    private final NamespaceDoc parent;
    private final String pageId;        
    private final Function<String, String> idGenerator;
    
    private Element elem;

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
        this.name = name;
        this.parent = parent;
        this.pageId = pageId;
        this.idGenerator = idGenerator;
    }        
    
    public void storeData(final Element elem) {                
        this.elem = elem;
    }
    
    public void writeBullet(WikiWriter writer, int level) throws IOException {
        
        writer.beginBullet(level);
        writer.internalLink(pageId, name);
        writer.text("\n");                
    }
    
    public void renderPage(WikiWriter writer) throws IOException {
                   
        writer.heading(1, getFullName());
        writer.newParagraph();
        
        writer.internalLink(getParent().getPageId(), "Parent namespace");
        writer.newParagraph();
        
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
    
    private Element getFirstElementByName(final Element parent, final String name) {
           
        NodeList childNodes = elem.getElementsByTagName(name);
        if (childNodes.getLength() > 0) {
            return (Element)childNodes.item(0);
        }      
        else {
            return null;
        }
    }

    private void renderDocElem(final WikiWriter writer, final Element docElem) throws IOException {
        
        NodeList children = docElem.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.TEXT_NODE) {
                writeTextContent(writer, child.getTextContent());
            }                
            else if (child.getNodeType() == Node.ELEMENT_NODE) {
                
                Element childElem = (Element)child;
                
                switch (child.getNodeName()) {
                    case "para":
                        writer.newParagraph();
                        renderDocElem(writer, childElem);
                        break;
                    case "see":
                        writeReference(writer, childElem.getAttribute("cref"));
                        break;
                    case "c":
                        writeCode(writer, child.getTextContent());
                        break;
                    case "a":
                        writer.text(" ");
                        writer.externalLink(URI.create(childElem.getAttribute("href")), childElem.getTextContent());
                        writer.text(" ");
                        break;
                }
            }
        }
        
        writer.newParagraph();
    }

    private void writeReference(WikiWriter writer, String cref) throws IOException {
    
        writer.text(" ");
        writer.internalLink(idGenerator.apply(cref), cref);
        writer.text(" ");
    }

    private void writeCode(WikiWriter writer, String code) throws IOException {
        writer.newParagraph();
        writer.sourceCode("csharp", code);
        writer.newParagraph();
    }

    private void writeTextContent(WikiWriter writer, String textContent) throws IOException {
        
        String[] lines = textContent.split("\n");
        boolean first = true;
        for (String line : lines) {
            if (!first)
                writer.text("\n");
            writer.text(line.trim());
            first = false;
        }
    }
}
