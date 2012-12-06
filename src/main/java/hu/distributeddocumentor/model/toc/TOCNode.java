package hu.distributeddocumentor.model.toc;

import hu.distributeddocumentor.model.ExportableNode;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.util.Collection;
import java.util.List;

public interface TOCNode {

    /**
     * Gets all the child nodes belonging to this node
     *
     * @return an unmodifiable list of nodes
     */
    List<TOCNode> getChildren();
        
    /**
     * Removes every children from this node
     */
    void clearChildren();

    /**
     * Gets the parent node
     *
     * @return the parent node, or null if this is the root node
     */
    TOCNode getParent();

    /**
     * Gets the page this node refers to
     *
     * @return the page the node refers to or null
     */
    Page getTarget();

    /**
     * Gets the title of this node (as it is shown in the TOC)
     *
     * @return the title of the node
     */
    String getTitle();

    /**
     * Checks if the node has a target page set
     *
     * @return true if there is a target page the node refers to
     */
    boolean hasTarget();

    /**
     * Sets the page this node refers to
     *
     * @param target the target page (can be null to remove reference)
     */
    void setTarget(Page target);

    /**
     * Sets the title of this node (as it will be shown in the TOC)
     *
     * @param title the title of the node
     */
    void setTitle(String title);

    /**
     * Converts this node to a node path
     *
     * @return an array where every array item belongs to one level, first item
     * being the root.
     */
    Object[] toPath();
    
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
    ExportableNode getRealNode(File repositoryRoot, DocumentorPreferences prefs);    
    
    /**
     * Gets the set of page identifiers which are referenced by this node or any
     * of the child nodes in the whole subtree of this node.
     *
     * @return a collection of string page identifiers
     */
    Collection<String> getReferencedPages();
    
    /**
     * Gets the interface for TOC operations, to be used by the TOC class
     * @return TOC node operations interface
     */
    TOCNodeOperations getOperations();
    
    /**
     * Gets the serialization interface for the TOC node
     * @return TOC node serialization interface
     */
    TOCNodeSerialization getSerialization();
    
}
