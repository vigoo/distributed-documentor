package hu.distributeddocumentor.model.toc;

import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.Page;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Table of Contents for the documentation with two special nodes
 * 
 * <p>
 * The special nodes are 'Unorganized' and 'Recycle bin'. Newly created pages
 * are put by default to the unorganized node, from where it can be moved to
 * any other location of the TOC. Pages which have content but not referenced
 * anymore are put to the recycle bin.
 * 
 * <p>
 * The TOC consists of a tree of {@link TOCNode} nodes.
 * <p>
 * It is also observable through the {@link TreeModelListener} interface.
 * 
 * @author Daniel Vigovszky
 * @see TOCNode
 */
public class TOC {
    
    private final TOCNode root;
    private final TOCNodeOperations rootOp;
    
    private final TOCNode unorganized;
    private final TOCNode recycleBin;    
    private final Documentation documentation;
    private final TOCNodeFactory factory;
    
    private final List<TreeModelListener> listeners = new LinkedList<>();
    
    private boolean modified;
            
    /**
     * Creates a new, empty TOC
     * 
     */
    public TOC(Documentation documentation, TOCNodeFactory factory) {
        this.documentation = documentation;
        this.factory = factory;
                
        root = factory.createNode("Root");        
        rootOp = factory.getOperations(root);
        
        unorganized = factory.createNode("Unorganized pages");
        recycleBin = factory.createNode("Recycle bin");
        
        rootOp.addToEnd(unorganized);
        rootOp.addToEnd(recycleBin);
    }

    /**
     * Gets the TOCNode factory associated with this TOC
     * @return the TOCNode factory
     */
    public TOCNodeFactory getFactory() {
        return factory;
    }   
    
    /**
     * Gets the root node
     * 
     * @return the root node. It is never null.
     */
    public TOCNode getRoot() {
        return root;
    }

    /**
     * Gets the special 'Unorganized' node
     * 
     * @return a node, never null.
     */
    public TOCNode getUnorganized() {
        return unorganized;
    }      
    
    /**
     * Gets the special 'Recycle bin' node
     * 
     * @return a node, never null.
     */
    public TOCNode getRecycleBin() {
        return recycleBin;
    }
    
    
    /**
     * Saves the TOC if it has been modified
     * 
     * @param targetDirectory target directory where the TOC's XML representation should be put
     * @throws FileNotFoundException
     * @throws TransformerConfigurationException
     * @throws TransformerException 
     */
    public void saveIfModified(File targetDirectory) throws FileNotFoundException, TransformerConfigurationException, TransformerException {
        if (modified) {
            save(targetDirectory);
        }
    }
    
    /**
     * Saves the TOC
     * 
     * <p>
     * The implementation currently uses an XML representation for the TOC in a 
     * single file.
     * 
     * @param targetDirectory target directory where the TOC's XML representation will be put
     * @throws FileNotFoundException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public void save(File targetDirectory) throws FileNotFoundException, TransformerConfigurationException, TransformerException {
        
        File target = new File(targetDirectory, "toc.xml");
                
        Document doc = new DocumentImpl();
        Element docRoot = doc.createElement("TOC");
        
        rootOp.remove(unorganized);
        rootOp.remove(recycleBin);
        try {                                            
            docRoot.appendChild(factory.getSerialization(root).toXML(doc));        
        }
        finally {
            rootOp.addToEnd(unorganized);
            rootOp.addToEnd(recycleBin);
        }
        
        doc.appendChild(docRoot);
        
        Source source = new DOMSource(doc);
        Result result = new StreamResult(target);
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", Integer.valueOf(4));
        
        Transformer xformer = transformerFactory.newTransformer();
        xformer.setOutputProperty(OutputKeys.INDENT, "yes");        
        xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");        
        
        xformer.transform(source, result);
                
        modified = false;
    }

    /**
     * Loads the TOC from its XML representation
     * 
     * @param sourceDirectory the directory where the TOC file was saved
     * @param documentation documentation the TOC belongs to, used to resolve page references
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws ClassNotFoundException
     */
    public void load(File sourceDirectory, Documentation documentation) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException {
        
        File source = new File(sourceDirectory, "toc.xml");
        
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(source);
        
        doc.getDocumentElement().normalize();
        
        factory.getSerialization(root).fromXML(doc.getDocumentElement().getFirstChild(), documentation, factory);
        rootOp.addToEnd(unorganized);
        rootOp.addToEnd(recycleBin);
        
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
        }
        
        modified = false;
    }

    /**
     * Adds a new node to the end of a parent node's child list
     * 
     * @param parent the parent node to modify
     * @param child the new child node to be added
     */
    public void addToEnd(TOCNode parent, TOCNode child) {
        
        if (parent == root) {
            factory.getOperations(parent).addBefore(unorganized, child);
            
        } else {            
            factory.getOperations(parent).addToEnd(child);
        }
        
        notifyInsert(parent, child);
    }
    
    private void notifyInsert(TOCNode parent, TOCNode child) {
        Object[] arr = new Object[1];
        arr[0] = child;
        int[] indices = new int[1];
        indices[0] = parent.getChildren().indexOf(child);
        
        TreeModelEvent evt = new TreeModelEvent(this, parent.toPath(), indices, arr);
        for (TreeModelListener listener : listeners) {
            listener.treeNodesInserted(evt);
        }
        
        // Adding items to the recycle bin does not count as a significant
        // modification, as it is not saved within the TOC
        if (parent != recycleBin) {
            modified = true;
        }
    }
    
    /**
     * Adds a new child node before an other node to the same parent
     * 
     * @param existingNode the node which will follow the new node
     * @param newChild the new child to be added
     */
    public void addBefore(TOCNode existingNode, TOCNode newChild) {
        
        TOCNode parent = existingNode.getParent();
        
        if (existingNode == recycleBin) {
            factory.getOperations(parent).addBefore(unorganized, newChild);
        } // keeping the two special nodes at the end
        else {
            factory.getOperations(parent).addBefore(existingNode, newChild);
        }
        
        notifyInsert(parent, newChild);        
    }

    /**
     * Adds a new node after an existing node to the same parent
     * 
     * @param existingNode the existing node which will be followed by the new one
     * @param newChild the new child node to be added
     */
    public void addAfter(TOCNode existingNode, TOCNode newChild) {
        
        // keeping the two special nodes at the end
        if (existingNode == unorganized ||
            existingNode == recycleBin) {
         
            addBefore(unorganized, newChild);
        } else {                    
            TOCNode parent = existingNode.getParent();

            factory.getOperations(parent).addAfter(existingNode, newChild);

            notifyInsert(parent, newChild);        
        }
    }

    
    /**
     * Removes a node from the tree
     * 
     * @param node the node to be removed
     */
    public void remove(TOCNode node) {
        
        TOCNode parent = node.getParent();
        int idx = parent.getChildren().indexOf(node);
        factory.getOperations(parent).remove(node);
        
        Object[] arr = new Object[1];
        arr[0] = node;
        int[] indices = new int[1];
        indices[0] = idx;
        
        TreeModelEvent evt = new TreeModelEvent(this, parent.toPath(), indices, arr);
        for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(evt);
        }
        
        // Removing nodes from recycle bin does not count 
        // as a modification to be saved
        if (parent != recycleBin) {
            modified = true;
        } else {
            // but removing a node from the recycle bin means permanent deletion
            // of the referred page
            
            if (node.hasTarget()) {
                
                Page page = node.getTarget();                
                documentation.deletePage(page);
            }
        }
    }
    
    /**
     * Removes the node which refers to the given page
     * 
     * @param page the page to look for
     */
    public void remove(Page page) {
        TOCNode node = rootOp.findReferenceTo(page);
        
        if (node != null) {
            remove(node);
        }        
    }

    /**
     * Adds a tree model listener
     * 
     * @param tl listener to be added
     */
    public void addTreeModelListener(TreeModelListener tl) {
        listeners.add(tl);
    }
    
    /**
     * Removes a tree model listener
     * 
     * @param tl listener to be removed
     */
    public void removeTreeModelListener(TreeModelListener tl) {
        listeners.remove(tl);
    }

    /**
     * Changes the title of a node in the TOC
     * 
     * @param node the node to be changed
     * @param title the new title of the node
     */
    public void changeNodeTitle(TOCNode node, String title) {
        
        if (node != root &&
            node != unorganized &&
            node != recycleBin) {
            
            node.setTitle(title);            

            TOCNode parent = node.getParent();
            int[] indices = new int[1];
            indices[0] = parent.getChildren().indexOf(node);
            Object[] objs = new Object[1];
            objs[0] = node;
            
            TreeModelEvent evt = new TreeModelEvent(this, 
                                                    parent.toPath(),
                                                    indices,
                                                    objs);
            for (TreeModelListener listener : listeners) {
                listener.treeNodesChanged(evt);
            }
            
            modified = true;
        }
        
    }
    
    /**
     * Change the target page a node refers to
     * 
     * @param node the node to be changed
     * @param target the new target page
     */
    public void changeNodeTarget(TOCNode node, Page target) {
        
        if (node != root &&
            node != unorganized &&
            node != recycleBin) {
            
            node.setTarget(target);
            
            TOCNode parent = node.getParent();
            int[] indices = new int[1];
            indices[0] = parent.getChildren().indexOf(node);
            Object[] objs = new Object[1];
            objs[0] = node;
            
            TreeModelEvent evt = new TreeModelEvent(this, 
                                                    parent.toPath(),
                                                    indices,
                                                    objs);
            for (TreeModelListener listener : listeners) {
                listener.treeNodesChanged(evt);
            }
            
            modified = true;
        }
    }
    
    /**
     * Converts a node to a 'virtual root' for a documentation hierarchy builder
     * 
     * @param node the node to be converted
     * @param hierarchyBuilder the hierarchy builder class to be used
     * @param relativeSource source path for the hierarchy builder
     */
    public void convertToVirtualRoot(TOCNode node, Class hierarchyBuilder, String relativeSource) {
        if (node != root &&
            node != unorganized &&
            node != recycleBin) {
            
            VirtualTOCNode vnode = factory.createVirtualNode();
            vnode.setTitle(node.getTitle());
            vnode.setVirtualHierarchyBuilder(hierarchyBuilder);
            vnode.setSourcePath(relativeSource);
            
            factory.getOperations(node).replace(vnode);
            
            TOCNode parent = vnode.getParent();
            int[] indices = new int[1];
            indices[0] = parent.getChildren().indexOf(vnode);
            Object[] objs = new Object[1];
            objs[0] = vnode;
            
            TreeModelEvent evt = new TreeModelEvent(this, 
                                                    parent.toPath(),
                                                    indices,
                                                    objs);
            for (TreeModelListener listener : listeners) {
                listener.treeNodesChanged(evt);
            }
            
            modified = true;
        }
    }

    /**
     * Moves one node up in the tree
     * 
     * <p>
     * If there are other nodes in the same level before the node,
     * it just moves within the same parent. Otherwise it is moved
     * to the parent level.
     * 
     * @param node the node to be moved
     */
    public void moveUp(TOCNode node) {
        
        if (node != root &&
            node != unorganized &&
            node != recycleBin &&
            node.getParent() != null &&
            node.getParent() != recycleBin) {
            
            
            TOCNode parent = node.getParent();
            int idx = parent.getChildren().indexOf(node);

            if (idx == 0 && parent != root) {
                
                remove(node);                
                addBefore(parent, node);
            } else if (idx > 0 || parent != root) {
                
                remove(node);                
                if (parent.getChildren().size() >= idx) {
                    addBefore(parent.getChildren().get(idx-1), node);
                }
                else {
                    addBefore(parent.getChildren().get(parent.getChildren().size()-1), node);
                }
            }
        }        
    }    
    
    /**
     * Moves one node down in the tree
     * 
     * <p>
     * If there are more child nodes on the same level after the given node,
     * it is just moved on the same level. Otherwise moved to the parent level.
     * 
     * @param node the node to be moved
     */
    public void moveDown(TOCNode node) {
        
        if (node != root &&
            node != unorganized &&
            node != recycleBin &&
            node.getParent() != null &&
            node.getParent() != recycleBin) {
            
            TOCNode parent = node.getParent();
            int idx = parent.getChildren().indexOf(node);
            int originalSize = parent.getChildren().size();
        
            remove(node);
                
            if (idx == (originalSize - 1) && parent != root) {
                addAfter(parent, node);

            } else {             
                
                if (idx > 0) {
                    addAfter(parent.getChildren().get(idx), node);
                }
                else {
                    addAfter(parent.getChildren().get(0), node);
                }
            }            
        }
    }
    
    /**
     * Moves one node left (to the parent level)
     * 
     * @param node the node to be moved
     */
    public void moveLeft(TOCNode node) {
     
        if (node != root &&
            node != unorganized &&
            node != recycleBin) {
            
            TOCNode parent = node.getParent();
            
            if (parent != null && 
                parent != recycleBin &&
                parent.getParent() != null) {
                
                remove(node);
                addBefore(parent, node);
            }            
        }
    }
    
    /**
     * Moves a node right (to become a child of the node above it)
     * 
     * @param node the node to be moved
     */
    public void moveRight(TOCNode node) {
        
        if (node != root &&
            node != unorganized &&
            node != recycleBin) {
            
            TOCNode parent = node.getParent();
            
            if (parent != null && 
                parent != recycleBin) {
        
                int idx = parent.getChildren().indexOf(node);
                if (idx > 0) {
                 
                    TOCNode prev = parent.getChildren().get(idx-1);
                    
                    remove(node);
                    addToEnd(prev, node);                    
                }                
            }
        }
    }

    /**
     * Clears the TOC
     */
    public void clear() {
        
        root.clearChildren();
        unorganized.clearChildren();
        recycleBin.clearChildren();
                
        rootOp.addToEnd(unorganized);
        rootOp.addToEnd(recycleBin);
        
        modified = false;
    }

    /**
     * Get a collection of all the referenced page's unique identifiers
     * @return a collection of string page identifiers
     */
    public Collection<String> getReferencedPages() {
        Set<String> pages = new HashSet<>();
        
        for (TOCNode child : root.getChildren()) {
            if (child != unorganized &&
                child != recycleBin) {
                
                pages.addAll(child.getReferencedPages());
            }
        }
        
        return pages;
    }

    /**
     * Finds the TOCNode that refers to the given page
     * @param page page to look for
     * @return returns the node that refers to the page, or null
     */
    public TOCNode findReferenceTo(Page page) {
        return rootOp.findReferenceTo(page);
    }

    /**
     * Finds out if there a page is reference by any of the TOC nodes
     * @param page page to look for
     * @return returns true if there is a node which refers to the page
     */
    public boolean isReferenced(Page page) {
        return rootOp.isReferenced(page);
    }

    /**
     * Adds a new node to the end of the unorganized nodes list
     * @param newNode node to be added
     */
    public void addUnorganized(TOCNode newNode) {
        factory.getOperations(unorganized).addToEnd(newNode);
    }

    /**
     * Checks whether a given page is referenced by a node in the recycle bin
     * @param existingPage page to look for
     * @return if true the node that refers to existingPage is currently in the
     *         recycle bin
     */
    public boolean isInRecycleBin(Page existingPage) {
        return factory.getOperations(recycleBin).isReferenced(existingPage);
    }

    /**
     * Removes a node from the recycle bin completely
     * @param existingPage page to look for
     */
    public void removeFromRecycleBin(Page existingPage) {
        factory.getOperations(recycleBin).removeReferenceTo(existingPage);
    }

    /**
     * Called when a page as been renamed
     */
    public void onPageRenamed() {
        for (TreeModelListener listener : listeners) {        
            listener.treeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
        }
    }
}
