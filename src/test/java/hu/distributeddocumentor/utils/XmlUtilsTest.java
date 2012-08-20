package hu.distributeddocumentor.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.*;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlUtilsTest {
    
    private final String xml = "<doc><assembly><name>SmartCore.General</name></assembly><item id='1'><item id='1.1'><other><item id='1.1.X.1'/></other></item><item id='1.2'><item id='1.2.1'/><item id='1.2.2'/></item></item></doc>";
    
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
    public void testItemsHierarchy() {
        
        Element item1 = XmlUtils.getSingleElement(doc.getDocumentElement(), "item");
        
        assertNotNull(item1);
        assertEquals("1", item1.getAttribute("id"));
                
        Element[] item1children = XmlUtils.getElements(item1, "item");
        assertEquals(2, item1children.length);
        
        Element item11 = (Element) item1children[0];
        Element item12 = (Element) item1children[1];
        
        assertEquals("1.1", item11.getAttribute("id"));
        assertEquals("1.2", item12.getAttribute("id"));
        
        Element[] item11children = XmlUtils.getElements(item11, "item");
        assertEquals(0, item11children.length);
        
        Element item111 = XmlUtils.getSingleElement(item11, "item");
        assertNull(item111);
        
        Element[] item12children = XmlUtils.getElements(item12, "item");
        assertEquals(2, item12children.length);
        
        Element item121 = item12children[0];
        Element item122 = item12children[1];
        
        assertEquals("1.2.1", item121.getAttribute("id"));
        assertEquals("1.2.2", item122.getAttribute("id"));
    }
    
    @Test
    public void selectByAttributeWorks() {
        
        Element item122 = XmlUtils.getSingleElement(doc.getDocumentElement(), "item[@id='1']/item[@id='1.2']/item[@id='1.2.2']");
        assertNotNull(item122);
        assertEquals("1.2.2", item122.getAttribute("id"));
    }
    
    @Test
    public void deepSearchByAttribute() {
        Element item122 = XmlUtils.getSingleElement(doc.getDocumentElement(), "//item[@id='1.2.2']");
        assertNotNull(item122);
        assertEquals("1.2.2", item122.getAttribute("id"));
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
