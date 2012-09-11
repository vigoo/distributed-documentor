package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import hu.distributeddocumentor.utils.XmlUtils;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ClassDoc extends DocItem {
    
    private final static Logger log = LoggerFactory.getLogger(ClassDoc.class);

    private final String name;
    private final NamespaceDoc parent;
    private final String pageId;        
    
    private final SortedSet<PropertyDoc> properties;
    private final SortedSet<MethodDoc> methods;

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

    public SortedSet<PropertyDoc> getProperties() {
        return properties;
    }        
    
    public SortedSet<MethodDoc> getMethods() {
        return methods;
    }
    
    public ClassDoc(NamespaceDoc parent, String name, String pageId, Function<String, String> idGenerator) {
        super(idGenerator);
        
        this.name = name;
        this.parent = parent;
        this.pageId = pageId;
        
        properties = new TreeSet<>(
                new Comparator<PropertyDoc>() {

                    @Override
                    public int compare(PropertyDoc o1, PropertyDoc o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    
                });
        
        methods = new TreeSet<>(
                new Comparator<MethodDoc>() {
                    
                    @Override
                    public int compare(MethodDoc o1, MethodDoc o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
    }        
        
    public void writeBullet(WikiWriter writer, int level) throws IOException {
        
        writer.beginBullet(level);
        writer.internalLink(pageId, name);
        writer.text("\n");                
    }
    
    public void renderPage(WikiWriter writer) {
                   
        try {
            StringBuilder headingBuilder = new StringBuilder();
            
            Element elem = getElem();
            Element reflection = null;
            boolean extendsObject = false;
            
            if (elem != null) {
                
                reflection = XmlUtils.getSingleElement(elem, "reflection");
                if (reflection != null) {
                    
                    boolean isClass = XmlUtils.isAttributeTrue(reflection, "is-class");                    
                    boolean isInterface = XmlUtils.isAttributeTrue(reflection, "is-interface");                    
                    boolean isValueType = XmlUtils.isAttributeTrue(reflection, "is-value-type");                    

                    boolean isEnum = false;
                    Element extendsNode = XmlUtils.getSingleElement(reflection, "extends");
                    if (extendsNode != null) {
                        isEnum = extendsNode.hasAttribute("type") &&
                                 "System.Enum".equals(extendsNode.getAttribute("type"));
                        extendsObject = extendsNode.hasAttribute("type") &&
                                 "System.Object".equals(extendsNode.getAttribute("type"));
                    }
                    
                    if (isClass) {
                        headingBuilder.append("Class ");
                    }
                    else if (isInterface) {
                        headingBuilder.append("Interface ");
                    }
                    else if (isValueType && isEnum) {
                        headingBuilder.append("Enum ");
                    }                              
                    else if (isValueType) {
                        headingBuilder.append("Value type ");
                    }
                }
            }
            headingBuilder.append(getFullName());
            
            Map<String, Element> genericArgs = new HashMap<>();
            if (reflection != null) {
                
                NodeList args = XmlUtils.getElements(reflection, "genericargs/arg");
                if (args != null) {                    
                    headingBuilder.append("<");
                                        
                    for (int i = 0; i < args.getLength(); i++) {
                        
                        Element arg = (Element)args.item(i);
                        String argName = arg.getAttribute("name");
                        
                        if (i > 0) {
                            headingBuilder.append(", ");
                        }
                        
                        headingBuilder.append(argName);                        
                        genericArgs.put(argName, arg);                        
                    }
                    
                    headingBuilder.append(">");
                }
            }
            
            writer.heading(1, headingBuilder.toString());
            writer.newParagraph();

            writer.internalLink(getParent().getPageId(), "Parent namespace: " + getParent().getFullName());
            writer.newParagraph();
            
            if (reflection != null) {
                Element extendsNode = XmlUtils.getSingleElement(reflection, "extends");
                if (extendsNode != null && !extendsObject) {
                    writer.beginBullet(1);
                    writer.bold("Extends:\n");
                    
                    StringBuilder builder = new StringBuilder();
                    renderType(extendsNode, builder, getParent().getAsPrefix());
                    
                    writer.beginBullet(2);
                    writeCode(writer, builder.toString());                                        
                    writer.text("\n");
                }
                
                NodeList interfaceNodes = XmlUtils.getElements(reflection, "implements/interface");
                    
                if (interfaceNodes != null && interfaceNodes.getLength() > 0) {

                    writer.beginBullet(1);
                    writer.bold("Implements:\n");

                    for (int i = 0; i < interfaceNodes.getLength(); i++) {

                        Element ifaceElem = (Element)interfaceNodes.item(i);

                        StringBuilder builder = new StringBuilder();
                        renderType(ifaceElem, builder, getParent().getAsPrefix());

                        writer.beginBullet(2);
                        writeCode(writer, builder.toString());
                        writer.text("\n");
                    }
                }                
                
                writer.newParagraph();
            }
            
            if (elem != null) {

                Element summaryElem = XmlUtils.getSingleElement(elem, "summary");
                if (summaryElem != null) {
                    writer.heading(2, "Summary");
                    renderDocElem(writer, summaryElem);
                }
            }
            
            if (elem != null) {
                
                NodeList typeParams = XmlUtils.getElements(elem, "typeparam");
                
                if (typeParams.getLength() > 0) {
                    writer.heading(2, "Type parameters");
                    
                    for (int i = 0; i < typeParams.getLength(); i++) {

                        Element typeParam = (Element)typeParams.item(i);
                        
                        String paramName = typeParam.getAttribute("name");
                        writer.bold(paramName);
                        writer.text(": ");
                        
                        renderDocElem(writer, typeParam);
                        
                        if (genericArgs.containsKey(paramName)) {
                            writer.newParagraph();                                                                                    
                            renderGenericConstraints(writer, genericArgs.get(paramName));
                        }
                        
                        writer.newParagraph();
                    }
                }
                
                renderInvariants(writer);
            }                       
                
            if (properties.size() > 0) {
                writer.heading(2, "Properties");
                renderPropertyBullets(writer);
            }
            
            if (methods.size() > 0) {
                writer.heading(2, "Methods");
                renderMethodBullets(writer);
            }

            if (elem != null) {
                Element remarksElem = XmlUtils.getSingleElement(elem, "remarks");
                if (remarksElem != null) {
                    writer.heading(2, "Remarks");
                    renderDocElem(writer, remarksElem);
                }

                NodeList seeAlsoElems = XmlUtils.getElements(elem, "seealso");
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
            
            if (properties.size() > 0 ||
                methods.size() > 0) {
                
                writer.heading(2, "Details");
                            
                if (properties.size() > 0) {
                    renderPropertyDetails(writer);                    
                }
                
                if (methods.size() > 0) {
                    renderMethodDetails(writer);
                }
            }                        
        }
        catch (IOException ex) {
            log.error("Failed to render class page: " + getName() + " because of: " + ex.getMessage());
            // TODO: write exception to log and generated page
        }            
    }

    private void renderPropertyBullets(WikiWriter writer) throws IOException {
        
        for (PropertyDoc prop : properties) {            
            prop.writeBullet(writer, 1);
        }
    }

    private void renderPropertyDetails(WikiWriter writer) throws IOException {
        for (PropertyDoc prop : properties) {            
            prop.renderDetails(writer, 3);
        }
    }
    
    private void renderMethodBullets(WikiWriter writer) throws IOException {
     
        for (MethodDoc method : methods) {
            method.writeBullet(writer, 1);
        }
    }
    
    private void renderMethodDetails(WikiWriter writer) throws IOException {
        
        for (MethodDoc method : methods) {
            method.renderDetails(writer, 3);
        }
    }

    private void renderInvariants(WikiWriter writer) throws IOException {
         
        NodeList invariantNodes = XmlUtils.getElements(getElem(), "invariant");
        if (invariantNodes.getLength() > 0) {
            
            writer.heading(2, "Object invariants");            
            
            StringBuilder ensurements = new StringBuilder();
            for (int i = 0; i < invariantNodes.getLength(); i++) {
                
                Node ensure = invariantNodes.item(i);       
                ensurements.append(ensure.getTextContent());
                ensurements.append(";\n");
            }
            
            writer.sourceCode("csharp", ensurements.toString());
        }
    }
    
    private void renderGenericConstraints(WikiWriter writer, Element arg) throws IOException {
        
        NodeList interfaces = XmlUtils.getElements(arg, "constraints/interface");
        
        if (arg.hasAttribute("must-be-reference-type") ||
            arg.hasAttribute("must-be-not-nullable-value-type") ||
            arg.hasAttribute("must-have-default-constructor") ||
            (interfaces != null && interfaces.getLength() > 0)) {
            
            writer.text("The type paramter has the following constraints:");
            writer.newParagraph();
        }
        
        if (XmlUtils.isAttributeTrue(arg, "must-be-reference-type")) {
            writer.beginBullet(1);
            writer.text("Must be a ");
            writer.bold("reference type");
            writer.text("\n");
        }
        
        if (XmlUtils.isAttributeTrue(arg, "must-be-not-nullable-value-type")) {
            writer.beginBullet(1);
            writer.text("Must be a ");
            writer.boldItalic("not nullable");
            writer.bold("value type");
            writer.text("\n");
        }
        
        if (XmlUtils.isAttributeTrue(arg, "must-have-default-constructor")) {
            writer.beginBullet(1);
            writer.text("Must have a ");
            writer.bold("default constructor");
            writer.text("\n");
        }
                
        if (interfaces != null) {
            for (int i = 0; i < interfaces.getLength(); i++) {

                writer.beginBullet(1);
                writer.text("Must implement interface: ");

                StringBuilder builder = new StringBuilder();
                renderType((Element)interfaces.item(i), builder, getParent().getAsPrefix());
                writeCode(writer, builder.toString());
                
                writer.text("\n");
            }
        }                
    }

}
