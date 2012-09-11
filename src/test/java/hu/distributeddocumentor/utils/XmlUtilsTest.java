package hu.distributeddocumentor.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.*;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlUtilsTest {
    
    private final String xml = "<doc><assembly><name>SmartCore.General</name></assembly></doc>";
    
    private Document doc;
    
    @Before
    public void setUp() throws SAXException, IOException, ParserConfigurationException {
       
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();        
        
        doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }
    
    @Test 
    public void getSingleDirectChild() {
               
        Element asm = XmlUtils.getSingleElement(doc.getDocumentElement(), "assembly");
        
        assertNotNull(asm);
        
        Element name = XmlUtils.getSingleElement(asm, "name");
        
        assertNotNull(name);
        assertEquals("SmartCore.General", name.getTextContent());
    }
    
    @Test
    public void getSingleDeepChild() {
        
        Element name = XmlUtils.getSingleElement(doc.getDocumentElement(), "assembly/name");
        
        assertNotNull(name);
        assertEquals("SmartCore.General", name.getTextContent());
    }
    
    @Test
    public void nonExistentChildReturnsNull() {
        
        Element path = XmlUtils.getSingleElement(doc.getDocumentElement(), "assembly/path");
        assertNull(path);
    }
}
