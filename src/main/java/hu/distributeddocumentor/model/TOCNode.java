package hu.distributeddocumentor.model;

import hu.distributeddocumentor.model.builders.UsesPreferences;
import hu.distributeddocumentor.model.virtual.VirtualHierarchyBuilder;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A node of the TOC tree
 * 
 * <p>
 * To modify TOC nodes use methods in the {@link TOC} class itself, so it can
 * notify its listeners about the change and maintain consistency.
 * 
 * @author Daniel Vigovszky
 * @see TOC
 */
public class TOCNode {
    // TODO: make virtual root node a subclass of TOCNode
    // TODO: hide the implementation details (methods which should be only called from TOC class) from the outside world
    //       by introducing a public TOCNode interface which only has some getters
    
    private static final Logger log = LoggerFactory.getLogger(Documentation.class.getName());
    private static final Collection<File> noExtraImages = new HashSet<>();
    
    private String title;
    private Page target;   
    private TOCNode parent;
    private final List<TOCNode> children;
    
    private Class virtualHierarchyBuilder;
    private String sourcePath;
    
    /**
     * Creates a new empty node
     */
    public TOCNode() {
        children = new LinkedList<>();
    }
    
    /**
     * Creates a new empty node with a title
     * @param title the title of the node
     */
    public TOCNode(String title) {
        this();
        
        this.title = title;
    }
    
    /**
     * Creates a new empty node with title 'Unitled', referring to a page
     * @param target the page the node refers to
     */
    public TOCNode(Page target) {
        this();
        
        this.title = "Untitled";
        this.target = target;
    }
    
    /**
     * Creates a new node with title and target page
     * @param title the title of the node
     * @param target the page this node refers to
     */
    public TOCNode(String title, Page target) {
        this();
        
        this.title = title;
        this.target = target;
    }
    
    /**
     * Gets all the child nodes belonging to this node
     * 
     * @return an unmodifiable list of nodes
     */
    public List<TOCNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Checks if the node has a target page set
     * 
     * @return true if there is a target page the node refers to
     */
    public boolean hasTarget() { 
        return target != null;
    }
    
    /**
     * Gets the page this node refers to
     * 
     * @return the page the node refers to or null
     */
    public Page getTarget() {
        return target;
    }

    /**
     * Sets the page this node refers to
     * @param target the target page (can be null to remove reference)
     */
    public void setTarget(Page target) {
        this.target = target;
    }

    /**
     * Gets the title of this node (as it is shown in the TOC)
     * @return the title of the node
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this node (as it will be shown in the TOC)
     * @param title the title of the node
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the source path passed for the hierarchy builder if this is a virtual
     * root node.
     * 
     * @return the source path, or null if this is no a virtual root node
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Sets the source path to be passed to the hierarchy builder when this node
     * is treated as a virtual root node.
     * 
     * @param sourcePath the source path to be passed
     */
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * Gets the type of the virtual hierarchy builder implementation to be used
     * to generate the child nodes of this virtual root node.
     * 
     * @return the type of the builder or null if this is no a virtual root node
     */
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
    public void setVirtualHierarchyBuilder(Class virtualHierarchyBuilder) {
        this.virtualHierarchyBuilder = virtualHierarchyBuilder;
    }        

    /**
     * Adds a new child node to the end of the child node list
     * 
     * @param child the node to be added
     */
    public void addToEnd(TOCNode child) {
        children.add(child);
        child.setParent(this);
    }
    
    /**
     * Adds a new child node before one of the existing child nodes
     * 
     * @param existingChild the existing child node
     * @param newChild the new node to be added
     */
    public void addBefore(TOCNode existingChild, TOCNode newChild) {
        children.add(children.indexOf(existingChild), newChild);
        newChild.setParent(this);
    }
    
    /**
     * Adds a new child node after one of the existing child nodes
     * 
     * @param existingChild the existing child node
     * @param newChild the new node to be added
     */
    public void addAfter(TOCNode existingChild, TOCNode newChild) {
        children.add(children.indexOf(existingChild)+1, newChild);
        newChild.setParent(this);
    }

    /**
     * Gets the parent node 
     * 
     * @return the parent node, or null if this is the root node
     */
    public TOCNode getParent() {
        return parent;
    }

    /**
     * Sets the parent node (called when the node is added to the hierarchy)
     * 
     * @param parent the new parent node for this node
     */
    public void setParent(TOCNode parent) {
        this.parent = parent;
    }
        

    @Override
    public String toString() {
        
        String targetRef = "";        
        if (target != null) {
            targetRef = " [" + target.getId() + "]";
        }
       
        if (title != null) {                        
            return title + targetRef;
        }
        else {
            return "Untitled" + targetRef;
        }
    }

    /**
     * Creates the node's XML representation for serialization
     * 
     * @param doc the XML document being constructed
     * @return returns the XML node representing this TOC node
     */
    public Node toXML(Document doc) {
        
        Element elem = doc.createElement("Node");
        elem.setAttribute("title", title);
        
        if (target != null) {
            elem.setAttribute("target", target.getId());
        }
        
        if (virtualHierarchyBuilder != null) {
            elem.setAttribute("virtual-hierarchy-builder", virtualHierarchyBuilder.getName());
            elem.setAttribute("source", sourcePath);
        }
        
        for (TOCNode node : children) {
            elem.appendChild(node.toXML(doc));
        }
        
        return elem;
    }

    /**
     * Loads the node from its XML representation
     * 
     * @param node the XML node representing this TOC node
     * @param doc the XML document being loaded
     * @throws ClassNotFoundException if the referenced virtual hierarchy builder
     *                                class does not exist
     */
    public void fromXML(Node node, Documentation doc) throws ClassNotFoundException {
        
        Node sibling;
        for (sibling = node; sibling != null; sibling = sibling.getNextSibling()) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                break;
            }
        }
        
        if (sibling != null) {
            Element elem = (Element)sibling;
            title = elem.getAttribute("title");        

            if (elem.hasAttribute("target")) {
                String targetId = elem.getAttribute("target");

                target = doc.getPage(targetId);

            } else {

                target = null;
            }
            
            if (elem.hasAttribute("virtual-hierarchy-builder")) {
                String className = elem.getAttribute("virtual-hierarchy-builder");
                sourcePath = elem.getAttribute("source");
                                
                virtualHierarchyBuilder = Class.forName(className);                
            }

            children.clear();

            NodeList childNodes = elem.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {

                    TOCNode child = new TOCNode();
                    child.fromXML(childNode, doc);
                    child.setParent(this);

                    children.add(child);
                }            
            }        
        }
    }     
    
    /**
     * Checks if the page or any of its children refers to the given page
     * 
     * @param page the page to look for
     * @return true if this node or any child node (recursively) refers to the page
     */
    public boolean isReferenced(Page page) {
        
        if (target == page) {
            return true;
        }
        
        for (TOCNode childNode : children) {
            if (childNode.isReferenced(page)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Removes one of the child nodes
     * 
     * @param child the child node to be removed
     */
    public void remove(TOCNode child) {
        children.remove(child);
    }
    
    /**
     * Removes a node from the whole subtree represented by this node
     * 
     * @param child the child node to be removed
     */
    public void deepRemove(TOCNode child) {
        children.remove(child);
        
        for (TOCNode childNode : children) {
            childNode.deepRemove(child);
        }
    }

    /**
     * Removes the node that refers to the given page from the whole subtree
     * represented by this node.
     * 
     * @param page the page to look for
     */
    public void removeReferenceTo(Page page) {
        
        Set<TOCNode> toRemove = new HashSet<>();        
        
        for (TOCNode childNode : children) {
            if (childNode.getTarget() == page) {
                toRemove.add(childNode);
            }
            
            childNode.removeReferenceTo(page);
        }
        
        for (TOCNode childNode : toRemove) {
            children.remove(childNode);
        }
    }
    
    /**
     * Find the child node in the subtree represented by this node which refers
     * to the given page.
     * 
     * @param page the page to look for
     * @return returns the node which refers to the page and belongs to the 
     *         subtree represented by this node, or null if there is no such
     *         node.
     */
    public TOCNode findReferenceTo(Page page) {
        
        if (target == page) {
            return this;
        }
        
        for (TOCNode child : children) {
            TOCNode result = child.findReferenceTo(page);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    
    /**
     * Converts this node to a node path
     * 
     * @return an array where every array item belongs to 
     *         one level, first item being the root.
     */
    public Object[] toPath() {
        
        Object[] parentPath = parent == null ? new Object[0] : parent.toPath();
        Object[] result = new Object[parentPath.length + 1];
        
        System.arraycopy(parentPath, 0, result, 0, parentPath.length);        
        result[parentPath.length] = this;
                
        return result;
    }
    
    /**
     * Gets the set of page identifiers which are referenced by this node or any of
     * the child nodes in the whole subtree of this node.
     * 
     * @return a collection of string page identifiers
     */
    public Collection<String> getReferencedPages() {
        Set<String> pages = new HashSet<>();
        
        if (target != null) {
            pages.add(target.getId());
        }
        
        for (TOCNode child : children) {
            pages.addAll(child.getReferencedPages());
        }
        
        return pages;
    }


    /**
     * Removes every children from this node
     */
    public void clearChildren() {
        children.clear();
    }
    
    /**
     * Gets the node to be used when generating the documentation
     * 
     * <p>
     * If this is a virtual root node, then this call will generate the subtree
     * with the hierarchy builder. Otherwise it just returns itself.
     * 
     * @param repositoryRoot the repository's root directory
     * @param prefs the application's preferences, the builder may need it
     * @return returns the node to be used when generating the documentation
     */
    public ExportableNode getRealNode(File repositoryRoot, DocumentorPreferences prefs) {
        
        if (virtualHierarchyBuilder == null) {
            return new ExportableNode(this, null, noExtraImages);
        }
        else {
            try{
                VirtualHierarchyBuilder builder = (VirtualHierarchyBuilder) ConstructorUtils.invokeConstructor(virtualHierarchyBuilder, new File(repositoryRoot, sourcePath), title, "MediaWiki");
                
                if (builder instanceof UsesPreferences) {
                    UsesPreferences up = (UsesPreferences)builder;
                    up.setPreferences(prefs);
                }                    
                
                TOCNode result = builder.build();
                if (result != null) {
                    return new ExportableNode(result, builder.getScope(), builder.getExtraImages());
                }
                else {
                    return new ExportableNode(this, null, noExtraImages);
                }
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
                log.error("Failed to create virtual hierarcby builder", ex);
               
                return new ExportableNode(this, null, noExtraImages);
            }
        }
    }
}
