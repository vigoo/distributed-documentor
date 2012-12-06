package hu.distributeddocumentor.model.toc;

import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.ExportableNode;
import hu.distributeddocumentor.model.builders.UsesPreferences;
import hu.distributeddocumentor.model.virtual.VirtualHierarchyBuilder;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


public class DefaultVirtualTOCNode extends DefaultTOCNode implements VirtualTOCNode {

    private static final Logger log = LoggerFactory.getLogger(DefaultVirtualTOCNode.class.getName());
    
    private final TOCNodeFactory factory;
    private Class virtualHierarchyBuilder;
    private String sourcePath;

    public DefaultVirtualTOCNode(TOCNodeFactory factory) {
        this.factory = factory;
    }
       
    
    /**
     * Gets the source path passed for the hierarchy builder if this is a virtual
     * root node.
     * 
     * @return the source path, or null if this is no a virtual root node
     */
    @Override
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Sets the source path to be passed to the hierarchy builder when this node
     * is treated as a virtual root node.
     * 
     * @param sourcePath the source path to be passed
     */
    @Override
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * Gets the type of the virtual hierarchy builder implementation to be used
     * to generate the child nodes of this virtual root node.
     * 
     * @return the type of the builder or null if this is no a virtual root node
     */
    @Override
    public Class getVirtualHierarchyBuilder() {
        return virtualHierarchyBuilder;
    }

    /**
     * Sets the type of the virtual hierarchy builder implementation to be used
     * to generate the child nodes of this virtual root node.
     * 
     * @param virtualHierarchyBuilder the type of the builder class implementing
     *                                {@link VirtualHierarchyBuilder}
     */
    @Override
    public void setVirtualHierarchyBuilder(Class virtualHierarchyBuilder) {
        this.virtualHierarchyBuilder = virtualHierarchyBuilder;
    }        

    @Override
    protected void fillXMLElement(Element elem) {
        super.fillXMLElement(elem);
 
        if (virtualHierarchyBuilder != null) {
            elem.setAttribute("virtual-hierarchy-builder", virtualHierarchyBuilder.getName());
            elem.setAttribute("source", sourcePath);
        } 
    }

    @Override
    protected void fromXMLElement(Element elem, Documentation doc) throws ClassNotFoundException {
        super.fromXMLElement(elem, doc);
                    
        if (elem.hasAttribute("virtual-hierarchy-builder")) {
            String className = elem.getAttribute("virtual-hierarchy-builder");
            sourcePath = elem.getAttribute("source");
                                
            virtualHierarchyBuilder = Class.forName(className);                
        }
    }

    @Override
    public ExportableNode getRealNode(File repositoryRoot, DocumentorPreferences prefs) {
        if (virtualHierarchyBuilder == null) {
            return new ExportableNode(this, null, noExtraImages);
        } else {
            try {
                VirtualHierarchyBuilder builder = (VirtualHierarchyBuilder) ConstructorUtils.invokeConstructor(virtualHierarchyBuilder, new File(repositoryRoot, sourcePath), getTitle(), "MediaWiki", factory);

                if (builder instanceof UsesPreferences) {
                    UsesPreferences up = (UsesPreferences) builder;
                    up.setPreferences(prefs);
                }

                TOCNode result = builder.build();
                if (result != null) {
                    return new ExportableNode(result, builder.getScope(), builder.getExtraImages());
                } else {
                    return new ExportableNode(this, null, noExtraImages);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
                log.error("Failed to create virtual hierarcby builder", ex);

                return new ExportableNode(this, null, noExtraImages);
            }
        }
    }       
}
