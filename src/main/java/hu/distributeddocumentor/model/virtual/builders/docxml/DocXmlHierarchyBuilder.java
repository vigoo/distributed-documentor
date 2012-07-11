package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOCNode;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import hu.distributeddocumentor.model.virtual.builders.VirtualHierarchyBuilderBase;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
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
    
        
    private final RootNamespaceDoc rootNS;
    private final Function<String, String> idGenerator;

    public DocXmlHierarchyBuilder(File xmlFile, String title, String markupLanguage) {
        super(markupLanguage);
        
        this.xmlFile = xmlFile;
        this.title = title;
        
        idGenerator = new Function<String, String>() {

            @Override
            public String apply(String input) {
                return generateId(rootNS.getAssemblyName(), input);
            }            
        };
        
        String rootId = idGenerator.apply("");
        rootNS = new RootNamespaceDoc(rootId, idGenerator);
    }   
    
    @Override
    public TOCNode build() {
        
        load();
        
        Page rootPage = virtualPage(rootNS.getPageId(), 
                new Function<WikiWriter, Void>() {

                    @Override
                    public Void apply(WikiWriter writer) {
                        rootNS.renderPage(writer);
                        return null;
                    }});
        
        TOCNode root = createNode(title, rootPage);
        SortedSet<NamespaceDoc> flatNamespaces = createFlatNamespaces();
        
        for (NamespaceDoc ns : flatNamespaces) {
            if (ns != rootNS) {                
                root.addToEnd(buildNamespaceNode(ns));
            }
        }
        
        return root;
           
    }

    private SortedSet<NamespaceDoc> createFlatNamespaces() {
        SortedSet<NamespaceDoc> flatNamespaces = new TreeSet<>(
                new Comparator<NamespaceDoc>() {
                    @Override
                    public int compare(NamespaceDoc o1, NamespaceDoc o2) {
                        return o1.getFullName().compareTo(o2.getFullName());
                    }});       
        fillFlatNamespaces(flatNamespaces, rootNS);
        return flatNamespaces;
    }
    
    private void fillFlatNamespaces(final SortedSet<NamespaceDoc> target, final NamespaceDoc current) {
        
        target.add(current);
        for (NamespaceDoc child : current.getChildNamespaces().values()) 
            fillFlatNamespaces(target, child);
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
            
            rootNS.setAssemblyName((String)xAsmName.evaluate(doc, XPathConstants.STRING));
            NodeList memberNodes = (NodeList)xMembers.evaluate(doc, XPathConstants.NODESET);
            
            log.debug("Assembly name: " + rootNS.getAssemblyName());
            log.debug("Number of documented members: " + memberNodes.getLength());
            
            for (int i = 0; i < memberNodes.getLength(); i++) {
                
                Element memberNode = (Element)memberNodes.item(i);
                
                String name = memberNode.getAttribute("name");
                if (name.startsWith("T:")) {
                    addClass(name.substring(2), memberNode);
                }
            }
        }
        catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            log.error("Failed to parse DocXml: " + ex);
        }
    }
    
    private ClassDoc addClass(final String fullClassName, final Element elem) {
        
        int lastDot = fullClassName.lastIndexOf('.');
        String nn = fullClassName.substring(0, lastDot);
        String cn = fullClassName.substring(lastDot+1);
        
        NamespaceDoc ns = buildNamespace(nn);
        ClassDoc cl = ns.addChildClass(cn, idGenerator.apply(fullClassName));
        cl.storeData(elem);
        
        return cl;
        
    }
    
    private NamespaceDoc buildNamespace(final String fullName) {
        
        return rootNS.buildSubtree(fullName);
    }


    private TOCNode buildNamespaceNode(final NamespaceDoc ns) {
        
        Page page = virtualPage(ns.getPageId(), 
                new Function<WikiWriter, Void>() {

                    @Override
                    public Void apply(WikiWriter writer) {
                        ns.renderPage(writer);
                        return null;
                    }           
        });
        
        TOCNode node = createNode(ns.getFullName(), page);        
        
        for (ClassDoc cl : ns.getSortedClasses()) {                    
            node.addToEnd(buildClassNode(cl));           
        }        
        
        return node;
    }        

    private TOCNode buildClassNode(final ClassDoc cl) {
         Page page = virtualPage(cl.getPageId(), 
                new Function<WikiWriter, Void>() {

                    @Override
                    public Void apply(WikiWriter writer) {
                        cl.renderPage(writer);
                        return null;
                    }
        });
        
        TOCNode node = createNode(cl.getName(), page);
        return node;
    }
}
