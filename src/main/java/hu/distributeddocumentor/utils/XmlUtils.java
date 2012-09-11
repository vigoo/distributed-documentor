package hu.distributeddocumentor.utils;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XmlUtils {

    private static XPath xpath = XPathFactory.newInstance().newXPath();     
    private static Map<String, XPathExpression> cache = new HashMap<>();
    
    public static Element getSingleElement(Element parent, String expression) {
        
        Element[] results = getElements(parent, expression);
        
        if (results.length > 0) { 
            return results[0];
        }
        else {
            return null;
        }
    }
    
    public static Element[] getElements(Element parent, String expression) {
                       
        boolean isSimple = true;
        for (int i = 0; i < expression.length(); i++) {
            if (!Character.isAlphabetic(expression.charAt(i))) {
                isSimple = false;
                break;
            }
        }
        
        if (isSimple) {
            return getDirectElements(parent, expression);
        }
        else {
            try {
                XPathExpression xquery;
                if (cache.containsKey(expression)) {
                    xquery = cache.get(expression);
                } else {            
                    xquery = xpath.compile(expression);
                    cache.put(expression, xquery);
                }

                return itemsOf((NodeList)xquery.evaluate(parent, XPathConstants.NODESET));
            } catch (XPathExpressionException ex) {
                return null;
            }
        }
    }
    
    public static Element[] getDirectElements(Element parent, String name) {
        
        NodeList children = parent.getChildNodes();        
        List<Element> result = new LinkedList<>();
        
        for (int i = 0; i < children.getLength(); i++) {
            
            Node node = children.item(i);
            if (node.getNodeName().equals(name)) {
                result.add((Element)node);
            }
        }       
        
        return result.toArray(new Element[0]);
    }
    
    
    public static boolean isAttributeTrue(Element elem, String attribute) {
        return elem.hasAttribute(attribute) &&
               "true".equals(elem.getAttribute(attribute));
    }
    
    public static Element[] itemsOf(final NodeList nodeList) {
        
        Element[] result = new Element[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++) {
            result[i] = (Element) nodeList.item(i);
        }
        
        return result;
    }

    public static void dumpElement(Logger log, Element elem) {
        
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StringWriter buffer = new StringWriter();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(elem), new StreamResult(buffer));
            
            log.debug(buffer.toString());
        }
        catch (TransformerFactoryConfigurationError | IllegalArgumentException | TransformerException ex) {
            log.error("Failed to dump element", ex);
        }
    }
}
