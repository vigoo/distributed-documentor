package hu.distributeddocumentor.model;

import hu.distributeddocumentor.model.virtual.VirtualHierarchyBuilder;
import java.io.File;
import java.util.*;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TOCNode {
    
    private static final Logger log = LoggerFactory.getLogger(Documentation.class.getName());
    
    private String title;
    private Page target;   
    private TOCNode parent;
    private final List<TOCNode> children;
    
    private Class virtualHierarchyBuilder;
    private String sourcePath;
    
    public TOCNode() {
        children = new LinkedList<TOCNode>();
    }
    
    public TOCNode(String title) {
        this();
        
        this.title = title;
    }
    
    public TOCNode(Page target) {
        this();
        
        this.title = "Untitled";
        this.target = target;
    }
    
    public TOCNode(String title, Page target) {
        this();
        
        this.title = title;
        this.target = target;
    }
    
    public List<TOCNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean hasTarget() { 
        return target != null;
    }
    
    public Page getTarget() {
        return target;
    }

    public void setTarget(Page target) {
        this.target = target;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public Class getVirtualHierarchyBuilder() {
        return virtualHierarchyBuilder;
    }

    public void setVirtualHierarchyBuilder(Class virtualHierarchyBuilder) {
        this.virtualHierarchyBuilder = virtualHierarchyBuilder;
    }        

    public void addToEnd(TOCNode child) {
        children.add(child);
        child.setParent(this);
    }
    
    public void addBefore(TOCNode existingChild, TOCNode newChild) {
        children.add(children.indexOf(existingChild), newChild);
        newChild.setParent(this);
    }
    
    public void addAfter(TOCNode existingChild, TOCNode newChild) {
        children.add(children.indexOf(existingChild)+1, newChild);
        newChild.setParent(this);
    }

    public TOCNode getParent() {
        return parent;
    }

    public void setParent(TOCNode parent) {
        this.parent = parent;
    }
        

    @Override
    public String toString() {
        
        String targetRef = "";        
        if (target != null)
            targetRef = " [" + target.getId() + "]";
       
        if (title != null)                        
            return title + targetRef;
        else
            return "Untitled" + targetRef;
    }

    public Node toXML(Document doc) {
        
        Element elem = doc.createElement("Node");
        elem.setAttribute("title", title);
        
        if (target != null)
            elem.setAttribute("target", target.getId());
        
        if (virtualHierarchyBuilder != null) {
            elem.setAttribute("virtual-hierarchy-builder", virtualHierarchyBuilder.getName());
            elem.setAttribute("source", sourcePath);
        }
        
        for (TOCNode node : children) {
            elem.appendChild(node.toXML(doc));
        }
        
        return elem;
    }

    public void fromXML(Node node, Documentation doc) throws ClassNotFoundException {
        
        Node sibling;
        for (sibling = node; sibling != null; sibling = sibling.getNextSibling()) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE)
                break;
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
    
    public boolean isReferenced(Page page) {
        
        if (target == page) 
            return true;
        
        for (TOCNode childNode : children) {
            if (childNode.isReferenced(page))
                return true;
        }
        
        return false;
    }

    public void remove(TOCNode child) {
        children.remove(child);
    }
    
    public void deepRemove(TOCNode child) {
        children.remove(child);
        
        for (TOCNode childNode : children) {
            childNode.deepRemove(child);
        }
    }

    public void removeReferenceTo(Page page) {
        
        Set<TOCNode> toRemove = new HashSet<TOCNode>();        
        
        for (TOCNode childNode : children) {
            if (childNode.getTarget() == page)
                toRemove.add(childNode);
            
            childNode.removeReferenceTo(page);
        }
        
        for (TOCNode childNode : toRemove)
            children.remove(childNode);
    }
    
    public TOCNode findReferenceTo(Page page) {
        
        if (target == page) 
            return this;
        
        for (TOCNode child : children) {
            TOCNode result = child.findReferenceTo(page);
            if (result != null)
                return result;
        }
        
        return null;
    }
    
    public Object[] toPath() {
        
        Object[] parentPath = parent == null ? new Object[0] : parent.toPath();
        Object[] result = new Object[parentPath.length + 1];
        
        System.arraycopy(parentPath, 0, result, 0, parentPath.length);        
        result[parentPath.length] = this;
                
        return result;
    }
    
    public Collection<String> getReferencedPages() {
        Set<String> pages = new HashSet<String>();
        
        if (target != null)
            pages.add(target.getId());
        
        for (TOCNode child : children) {
            pages.addAll(child.getReferencedPages());
        }
        
        return pages;
    }


    public void clearChildren() {
        children.clear();
    }
    
    public TOCNode getRealNode(File repositoryRoot) {
        
        if (virtualHierarchyBuilder == null) {
            return this;
        }
        else {
            try{
                VirtualHierarchyBuilder builder = (VirtualHierarchyBuilder) ConstructorUtils.invokeConstructor(virtualHierarchyBuilder, new File(repositoryRoot, sourcePath), title, "MediaWiki");
                
                return builder.build();
            }
            catch (Exception ex) {
                log.error("Failed to create virtual hierarcby builder", ex);
               
                return this;
            }
        }
    }
}
