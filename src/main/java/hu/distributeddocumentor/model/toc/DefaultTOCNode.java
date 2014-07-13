package hu.distributeddocumentor.model.toc;

import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.ExportableNode;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.virtual.builders.VirtualNodeException;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A node of the TOC tree
 *
 * <p> To modify TOC nodes use methods in the {@link TOC} class itself, so it
 * can notify its listeners about the change and maintain consistency.
 *
 * @author Daniel Vigovszky
 * @see TOC
 */
public class DefaultTOCNode implements TOCNode, TOCNodeOperations, TOCNodeSerialization {
    protected static final Collection<File> noExtraImages = new HashSet<>();
    
    protected final TOCNodeFactory factory;
    private String title;
    private Page target;
    private TOCNode parent;
    private final List<TOCNode> children;

    /**
     * Creates a new empty node
     * 
     * @param factory the TOC node factory instance to be used
     */
    public DefaultTOCNode(TOCNodeFactory factory) {
        this.factory = factory;
        children = new LinkedList<>();
    }

    /**
     * Creates a new empty node with a title
     *
     * @param title the title of the node
     * @param factory the TOC node factory instance to be used
     */
    public DefaultTOCNode(TOCNodeFactory factory, String title) {
        this(factory);

        this.title = title;
    }

    /**
     * Creates a new empty node with title 'Unitled', referring to a page
     *
     * @param target the page the node refers to
     * @param factory the TOC node factory instance to be used
     */
    public DefaultTOCNode(TOCNodeFactory factory, Page target) {
        this(factory);

        this.title = "Untitled";
        this.target = target;
    }

    /**
     * Creates a new node with title and target page
     *
     * @param factory the TOC node factory instance to be used
     * @param title the title of the node
     * @param target the page this node refers to
     */
    public DefaultTOCNode(TOCNodeFactory factory, String title, Page target) {
        this(factory);

        this.title = title;
        this.target = target;
    }

    /**
     * Gets all the child nodes belonging to this node
     *
     * @return an unmodifiable list of nodes
     */
    @Override
    public List<TOCNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Checks if the node has a target page set
     *
     * @return true if there is a target page the node refers to
     */
    @Override
    public boolean hasTarget() {
        return target != null;
    }

    /**
     * Gets the page this node refers to
     *
     * @return the page the node refers to or null
     */
    @Override
    public Page getTarget() {
        return target;
    }

    /**
     * Sets the page this node refers to
     *
     * @param target the target page (can be null to remove reference)
     */
    @Override
    public void setTarget(Page target) {
        this.target = target;
    }

    /**
     * Gets the title of this node (as it is shown in the TOC)
     *
     * @return the title of the node
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this node (as it will be shown in the TOC)
     *
     * @param title the title of the node
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Adds a new child node to the end of the child node list
     *
     * @param child the node to be added
     */
    @Override
    public void addToEnd(TOCNode child) {
        children.add(child);
        factory.getOperations(child).setParent(this);
    }

    /**
     * Adds a new child node before one of the existing child nodes
     *
     * @param existingChild the existing child node
     * @param newChild the new node to be added
     */
    @Override
    public void addBefore(TOCNode existingChild, TOCNode newChild) {
        children.add(children.indexOf(existingChild), newChild);
        factory.getOperations(newChild).setParent(this);
    }

    /**
     * Adds a new child node after one of the existing child nodes
     *
     * @param existingChild the existing child node
     * @param newChild the new node to be added
     */
    @Override
    public void addAfter(TOCNode existingChild, TOCNode newChild) {
        children.add(children.indexOf(existingChild) + 1, newChild);
        factory.getOperations(newChild).setParent(this);
    }

    /**
     * Gets the parent node
     *
     * @return the parent node, or null if this is the root node
     */
    @Override
    public TOCNode getParent() {
        return parent;
    }

    /**
     * Sets the parent node (called when the node is added to the hierarchy)
     *
     * @param parent the new parent node for this node
     */
    @Override
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
        } else {
            return "Untitled" + targetRef;
        }
    }

    /**
     * Creates the node's XML representation for serialization
     *
     * @param doc the XML document being constructed
     * @return returns the XML node representing this TOC node
     */
    @Override
    public Node toXML(Document doc) {

        Element elem = doc.createElement("Node");
        fillXMLElement(elem);

        for (TOCNode node : children) {
            elem.appendChild(factory.getSerialization(node).toXML(doc));
        }

        return elem;
    }

    /**
     * Fills an XML Element with the node specific data
     *
     * @param elem element to fill with information
     */
    protected void fillXMLElement(Element elem) {
        elem.setAttribute("title", title);

        if (target != null) {
            elem.setAttribute("target", target.getId());
        }
    }

    /**
     * Loads the node from its XML representation
     *
     * @param node the XML node representing this TOC node
     * @param doc the XML document being loaded
     * @param factory the TOCNode class factory
     * @throws ClassNotFoundException if the referenced virtual hierarchy
     * builder class does not exist
     */
    @Override
    public void fromXML(Node node, Documentation doc, TOCNodeFactory factory) throws ClassNotFoundException {

        Node sibling;
        for (sibling = node; sibling != null; sibling = sibling.getNextSibling()) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                break;
            }
        }

        if (sibling != null) {
            Element elem = (Element) sibling;

            fromXMLElement(elem, doc);

            children.clear();

            NodeList childNodes = elem.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {

                    TOCNode child = factory.fromXML(childNode);
                    factory.getSerialization(child).fromXML(childNode, doc, factory);
                    factory.getOperations(child).setParent(this);

                    children.add(child);
                }
            }
        }
    }

    protected void fromXMLElement(Element elem, Documentation doc) throws ClassNotFoundException {
        title = elem.getAttribute("title");

        if (elem.hasAttribute("target")) {
            String targetId = elem.getAttribute("target");

            target = doc.getPage(targetId);

        } else {
            target = null;
        }
    }

    /**
     * Checks if the page or any of its children refers to the given page
     *
     * @param page the page to look for
     * @return true if this node or any child node (recursively) refers to the
     * page
     */
    @Override
    public boolean isReferenced(Page page) {

        if (target == page) {
            return true;
        }

        for (TOCNode childNode : children) {
            if (factory.getOperations(childNode).isReferenced(page)) {
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
    @Override
    public void remove(TOCNode child) {
        children.remove(child);
    }

    /**
     * Removes a node from the whole subtree represented by this node
     *
     * @param child the child node to be removed
     */
    @Override
    public void deepRemove(TOCNode child) {
        children.remove(child);

        for (TOCNode childNode : children) {
            factory.getOperations(childNode).deepRemove(child);
        }
    }

    /**
     * Removes the node that refers to the given page from the whole subtree
     * represented by this node.
     *
     * @param page the page to look for
     */
    @Override
    public void removeReferenceTo(Page page) {

        Set<TOCNode> toRemove = new HashSet<>();

        for (TOCNode childNode : children) {
            if (childNode.getTarget() == page) {
                toRemove.add(childNode);
            }

            factory.getOperations(childNode).removeReferenceTo(page);
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
     * subtree represented by this node, or null if there is no such node.
     */
    @Override
    public TOCNode findReferenceTo(Page page) {

        if (target == page) {
            return this;
        }

        for (TOCNode child : children) {
            TOCNode result = factory.getOperations(child).findReferenceTo(page);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
    
    /**
     * Replaces this node to a new instance
     * 
     * Useful for converting one node to another type
     * @param newNode The new node
     */
    @Override
    public void replace(TOCNode newNode) {
        factory.getOperations(parent).replaceChild(this, newNode);
    }
    
    /**
     * Replaces one of the direct child nodes to another instance
     * @param child child to be replaced
     * @param newChild replacement node
     */
    @Override
    public void replaceChild(TOCNode child, TOCNode newChild) {
        int idx = children.indexOf(child);
        if (idx >= 0) {
            children.remove(idx);
            children.add(idx, newChild);
            factory.getOperations(newChild).setParent(this);
        }
    }

    /**
     * Converts this node to a node path
     *
     * @return an array where every array item belongs to one level, first item
     * being the root.
     */
    @Override
    public Object[] toPath() {

        Object[] parentPath = parent == null ? new Object[0] : parent.toPath();
        Object[] result = new Object[parentPath.length + 1];

        System.arraycopy(parentPath, 0, result, 0, parentPath.length);
        result[parentPath.length] = this;

        return result;
    }

    /**
     * Gets the set of page identifiers which are referenced by this node or any
     * of the child nodes in the whole subtree of this node.
     *
     * @return a collection of string page identifiers
     */
    @Override
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
    @Override
    public void clearChildren() {
        children.clear();
    }

    /**
     * Gets the node to be used when generating the documentation
     *
     * <p> If this is a virtual root node, then this call will generate the
     * subtree with the hierarchy builder. Otherwise it just returns itself.
     *
     * @param repositoryRoot the repository's root directory
     * @param prefs the application's preferences, the builder may need it
     * @return returns the node to be used when generating the documentation
     */
    @Override
    public ExportableNode getRealNode(File repositoryRoot, DocumentorPreferences prefs) throws VirtualNodeException {

        return new ExportableNode(this, null, noExtraImages);        
    }
}
