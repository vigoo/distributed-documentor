package hu.distributeddocumentor.utils;

import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class XmlUtils {

    private static XPath xpath = XPathFactory.newInstance().newXPath();     
    private static Map<String, XPathExpression> cache = new HashMap<>();
    
    public static Element getSingleElement(Element parent, String name) {
        
        NodeList results = getElements(parent, name);
        if (results == null || results.getLength() == 0) {
            return null;
        }
        else {
            return (Element)results.item(0);
        }
    }
    
    public static NodeList getElements(Element parent, String expression) {
        try {
            XPathExpression xquery;
            if (cache.containsKey(expression)) {
                xquery = cache.get(expression);
            } else {            
                xquery = xpath.compile(expression);
            }
            
            return (NodeList)xquery.evaluate(parent, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            return null;
        }
    }
    
    public static boolean isAttributeTrue(Element elem, String attribute) {
        return elem.hasAttribute(attribute) &&
               "true".equals(elem.getAttribute(attribute));
    }
}
