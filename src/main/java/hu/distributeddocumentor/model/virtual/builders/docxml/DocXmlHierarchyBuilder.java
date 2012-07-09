package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOCNode;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import hu.distributeddocumentor.model.virtual.builders.VirtualHierarchyBuilderBase;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class DocXmlHierarchyBuilder extends VirtualHierarchyBuilderBase {
    
    private static final Logger log = LoggerFactory.getLogger(DocXmlHierarchyBuilder.class);
    
    private final File xmlFile;    
    private final String title;
    
    private String assemblyName;
    
    private final String rootId;
    private final RootNamespaceDoc rootNS;

    public DocXmlHierarchyBuilder(File xmlFile, String title, String markupLanguage) {
        super(markupLanguage);
        
        this.xmlFile = xmlFile;
        this.title = title;
        
        rootId = generateId();
        rootNS = new RootNamespaceDoc();
    }   
    
    @Override
    public TOCNode build() {
        
        load();
        
        Page rootPage = virtualPage(rootId, 
                new Function<WikiWriter, Void>() {

                    @Override
                    public Void apply(WikiWriter writer) {
                        renderRootPage(writer);
                        return null;
                    }});
        
        TOCNode root = createNode(title, rootPage);
        // TODO: add flat namespace nodes
        
        return root;
           
    }
    
    private void load() {
          
        try {            
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            doc.getDocumentElement().normalize();
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            XPathExpression xAsmName = xpath.compile("/doc/assembly/name");
            XPathExpression xMembers = xpath.compile("/doc/members/member");
            
            assemblyName = (String)xAsmName.evaluate(doc, XPathConstants.STRING);
            NodeList memberNodes = (NodeList)xMembers.evaluate(doc, XPathConstants.NODESET);
            
            log.debug("Assembly name: " + assemblyName);
            log.debug("Number of documented members: " + memberNodes.getLength());
            
            for (int i = 0; i < memberNodes.getLength(); i++) {
                
                Element memberNode = (Element)memberNodes.item(i);
                
                String name = memberNode.getAttribute("name");
                if (name.startsWith("T:")) {
                    addClass(name.substring(2));
                }
            }
        }
        catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            log.error("Failed to parse DocXml: " + ex);
        }
    }
    
    private ClassDoc addClass(String fullClassName) {
        
        int lastDot = fullClassName.lastIndexOf('.');
        String nn = fullClassName.substring(0, lastDot);
        String cn = fullClassName.substring(lastDot+1);
        
        NamespaceDoc ns = buildNamespace(nn);
        return ns.addChildClass(cn);
    }
    
    private NamespaceDoc buildNamespace(String fullName) {
        
        return rootNS.buildSubtree(fullName);
    }
                
    private void renderRootPage(WikiWriter writer) {
        
        try {
            writer.heading(1, assemblyName); 
            writer.newParagraph();
            writer.text("This is the root of the assembly's reference manual.\n");
            writer.text("The manual is structured by ");
            writer.italic("namespaces");
            writer.text(" containing ");
            writer.italic("classes");
            writer.text(". There is one documentation page for each class, describing\n");
            writer.text("all the fields, properties, events and methods of the class.");
            writer.newParagraph();
            
            for (NamespaceDoc ns : rootNS.getChildNamespaces().values()) {
                ns.writeBullet(writer, 1);
            }
            writer.newParagraph();
        }
        catch (IOException ex) {
            // TODO: write exception to log and generated page
        }
    }

}
