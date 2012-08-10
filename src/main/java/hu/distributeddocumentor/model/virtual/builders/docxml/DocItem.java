package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DocItem {

    protected final Function<String, String> idGenerator;    
    private Element elem;

    public DocItem(Function<String, String> idGenerator) {
        this.idGenerator = idGenerator;
    }
    
    protected Element getElem() {
        return elem;
    }
    
    public void storeData(final Element elem) {                
        this.elem = elem;
    }    
    
    protected Element getFirstElementByName(final Element parent, final String name) {
           
        NodeList childNodes = elem.getElementsByTagName(name);
        if (childNodes.getLength() > 0) {
            return (Element)childNodes.item(0);
        }      
        else {
            return null;
        }
    }

    protected void renderDocElem(final WikiWriter writer, final Element docElem) throws IOException {
        
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

    protected void writeReference(WikiWriter writer, String cref) throws IOException {
    
        String link;
        
        if (cref.startsWith("P:")) {
            cref = cref.substring(2);
            
            int classPropertySep = cref.lastIndexOf('.');
            String fullClassName = cref.substring(0, classPropertySep);
            String propertyName = cref.substring(classPropertySep+1);
            
            link = idGenerator.apply(fullClassName) + "#" + propertyName;            
            
        }
        else {
            cref = StringUtils.removeStart(cref, "N:");
            cref = StringUtils.removeStart(cref, "T:");

            link = idGenerator.apply(cref);
        }
        
        writer.text(" ");
        writer.internalLink(link, cref);
        writer.text(" ");

    }

    protected void writeCode(WikiWriter writer, String code) throws IOException {
        
        writer.text(" <span class=\"inlinecode\">");
        writer.text(code);
        writer.text("</span> ");
    }

    protected void writeTextContent(WikiWriter writer, String textContent) throws IOException {
        
        String[] lines = textContent.split("\n");
        boolean first = true;
        for (String line : lines) {
            if (!first)
                writer.text("\n");
            writer.text(line.trim());
            first = false;
        }
    }
    
    protected void renderType(Element typeElem, StringBuilder builder) {
        
        if (typeElem.hasAttribute("type")) {
            
            // TODO: convert to native representation if possible (System.Double to double, etc.)
            builder.append(typeElem.getAttribute("type"));
            
        } else {
            // TODO: support generic types
        }
    }
}
