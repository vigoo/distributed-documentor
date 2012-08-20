package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import hu.distributeddocumentor.utils.XmlUtils;
import java.io.IOException;
import java.net.URI;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DocItem {

    private final static Logger log = LoggerFactory.getLogger(DocItem.class);
    
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
            if (!first) {
                writer.text("\n");
            }
            
            writer.text(line.trim());
            first = false;
        }
    }
    
    protected void renderType(Element typeElem, StringBuilder builder, String currentNameSpacePrefix) {
        
        if (typeElem.hasAttribute("type")) {
                             
            String type = typeElem.getAttribute("type");
                        
            renderTypeName(type, currentNameSpacePrefix, builder);
           
        } else if (typeElem.hasAttribute("generic-type")) {
            
            String genericType = typeElem.getAttribute("generic-type");
            
            renderTypeName(genericType, currentNameSpacePrefix, builder);
            builder.append("<");

            XPath xpath = XPathFactory.newInstance().newXPath();            
            try {
                XPathExpression xInterfaces = xpath.compile("genericargs/arg");
                NodeList args = (NodeList)xInterfaces.evaluate(typeElem, XPathConstants.NODESET);               
                
                for (int i = 0; i < args.getLength(); i++) {

                    if (i > 0) {
                        builder.append(", ");
                    }

                   renderType((Element)args.item(i), builder, currentNameSpacePrefix);                                                  
                }
            }                                
            catch (XPathExpressionException ex) {
                log.error(ex.toString());
                // TODO: write to output
            }            
            
            builder.append(">");            
        }
        else if (typeElem.hasAttribute("name")) {
            builder.append(typeElem.getAttribute("name"));
        }
        else {
            builder.append("?");
        }
    }       
    
    protected void renderContract(WikiWriter writer, Element contractRootElem) throws IOException {
        
        Element[] requiresNodes = XmlUtils.getElements(contractRootElem, "requires");
        if (requiresNodes.length > 0) {
            
            writer.text("The caller has to meet the following ");
            writer.bold("requirements:");
            writer.newParagraph();
            
            StringBuilder requirements = new StringBuilder();
            for (Element requires : requiresNodes) {
                                
                requirements.append(requires.getTextContent());
                requirements.append(";\n");
            }
            
            writer.sourceCode("csharp", requirements.toString());
        }
        
        Element[] ensuresNodes = XmlUtils.getElements(contractRootElem, "ensures");
        if (ensuresNodes.length > 0) {
            
            writer.text("The callee ");
            writer.bold("ensures");
            writer.text(" that the following will be true:");
            writer.newParagraph();
            
            StringBuilder ensurements = new StringBuilder();
            for (Element ensure : ensuresNodes) {
                                
                ensurements.append(ensure.getTextContent());
                ensurements.append(";\n");
            }
            
            writer.sourceCode("csharp", ensurements.toString());
        }
    }
    
    private String toCsharpType(String type) {
        
        switch (type) {
            case "System.Object":
                return "object";
            case "System.Boolean":
                return "bool";
            case "System.Int32":
                return "int";
            case "System.Int32[]":
                return "int[]";
            case "System.String":
                return "string";
            case "System.String[]":
                return "string[]";
            case "System.Double":
                return "double";
            case "System.Double[]":
                return "double[]";
            case "System.Void":
                return "void";
                
            default:
                return type;
        }
    }

    private void renderTypeName(String type, String currentNameSpacePrefix, StringBuilder builder) {
        if (type.startsWith(currentNameSpacePrefix)) {             
            builder.append(type.substring(currentNameSpacePrefix.length()));
        } else
        {
            builder.append(toCsharpType(type));
        }
    }
}
