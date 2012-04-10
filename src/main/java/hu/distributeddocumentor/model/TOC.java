package hu.distributeddocumentor.model;

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
 *
 * @author vigoo
 */
public class TOC {
    
    private final TOCNode root;
    private final TOCNode unorganized;
    private final TOCNode recycleBin;    
    
    private final List<TreeModelListener> listeners = new LinkedList<TreeModelListener>();
    
    private boolean modified;
            
    public TOC() {
        root = new TOCNode("Root");
        
        unorganized = new TOCNode("Unorganized pages");
        recycleBin = new TOCNode("Recycle bin");
        
        root.addToEnd(unorganized);
        root.addToEnd(recycleBin);
    }

    public TOCNode getRoot() {
        return root;
    }

    public TOCNode getUnorganized() {
        return unorganized;
    }      
    
    public TOCNode getRecycleBin() {
        return recycleBin;
    }
    
    
    void saveIfModified(File targetDirectory) throws FileNotFoundException, TransformerConfigurationException, TransformerException {
        if (modified)
            save(targetDirectory);
    }
    
    public void save(File targetDirectory) throws FileNotFoundException, TransformerConfigurationException, TransformerException {
        
        File target = new File(targetDirectory, "toc.xml");
                
        Document doc = new DocumentImpl();
        Element docRoot = doc.createElement("TOC");
        
        root.remove(unorganized);
        root.remove(recycleBin);
        try {                                            
            docRoot.appendChild(root.toXML(doc));        
        }
        finally {
            root.addToEnd(unorganized);
            root.addToEnd(recycleBin);
        }
        
        doc.appendChild(docRoot);
        
        Source source = new DOMSource(doc);
        Result result = new StreamResult(target);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute("indent-number", new Integer(4));
        
        Transformer xformer = factory.newTransformer();
        xformer.setOutputProperty(OutputKeys.INDENT, "yes");        
        xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");        
        
        xformer.transform(source, result);
        
        modified = false;
    }

    public void load(File sourceDirectory, Documentation documentation) throws SAXException, IOException, ParserConfigurationException {
        
        File source = new File(sourceDirectory, "toc.xml");
        
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document doc = builder.parse(source);
        
        doc.getDocumentElement().normalize();
        
        root.fromXML(doc.getDocumentElement().getFirstChild(), documentation);
        root.addToEnd(unorganized);
        root.addToEnd(recycleBin);
        
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
        }
        
        modified = false;
    }

    public void addToEnd(TOCNode parent, TOCNode child) {
        
        if (parent == root) {
            parent.addBefore(unorganized, child);
            
        } else {            
            parent.addToEnd(child);
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
        
        modified = true;
    }
    
    public void addBefore(TOCNode existingNode, TOCNode newChild) {
        
        TOCNode parent = existingNode.getParent();
        
        if (existingNode == recycleBin)
            parent.addBefore(unorganized, newChild); // keeping the two special nodes at the end
        else
            parent.addBefore(existingNode, newChild);
        
        notifyInsert(parent, newChild);        
    }

    public void addAfter(TOCNode existingNode, TOCNode newChild) {
        
        // keeping the two special nodes at the end
        if (existingNode == unorganized ||
            existingNode == recycleBin) {
         
            addBefore(unorganized, newChild);
        } else {                    
            TOCNode parent = existingNode.getParent();

            parent.addAfter(existingNode, newChild);

            notifyInsert(parent, newChild);        
        }
    }

    
    public void remove(TOCNode node) {
        
        TOCNode parent = node.getParent();
        int idx = parent.getChildren().indexOf(node);
        parent.remove(node);
        
        Object[] arr = new Object[1];
        arr[0] = node;
        int[] indices = new int[1];
        indices[0] = idx;
        
        TreeModelEvent evt = new TreeModelEvent(this, parent.toPath(), indices, arr);
        for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(evt);
        }
        
        modified = true;
    }
    
    public void remove(Page page) {
        TOCNode node = root.findReferenceTo(page);
        
        if (node != null) {
            remove(node);
        }        
    }

    public void addTreeModelListener(TreeModelListener tl) {
        listeners.add(tl);
    }
    
    public void removeTreeModelListener(TreeModelListener tl) {
        listeners.remove(tl);
    }

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
                if (parent.getChildren().size() >= idx)
                    addBefore(parent.getChildren().get(idx-1), node);
                else
                    addBefore(parent.getChildren().get(parent.getChildren().size()-1), node);
            }
        }        
    }    
    
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
                
                if (idx > 0)
                    addAfter(parent.getChildren().get(idx), node);
                else
                    addAfter(parent.getChildren().get(0), node);
            }            
        }
    }
    
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

    public void clear() {
        
        root.clearChildren();
        unorganized.clearChildren();
        recycleBin.clearChildren();
                
        root.addToEnd(unorganized);
        root.addToEnd(recycleBin);
        
        modified = false;
    }

    public Collection<String> getReferencedPages() {
        Set<String> pages = new HashSet<String>();
        
        for (TOCNode child : root.getChildren()) {
            if (child != unorganized &&
                child != recycleBin) {
                
                pages.addAll(child.getReferencedPages());
            }
        }
        
        return pages;
    }
}
