package hu.distributeddocumentor.model.toc;

import hu.distributeddocumentor.model.Page;


public interface TOCNodeOperations {
    
    /**
     * Sets the parent node (called when the node is added to the hierarchy)
     *
     * @param parent the new parent node for this node
     */
    void setParent(TOCNode parent);
    
    /**
     * Adds a new child node after one of the existing child nodes
     *
     * @param existingChild the existing child node
     * @param newChild the new node to be added
     */
    void addAfter(TOCNode existingChild, TOCNode newChild);

    /**
     * Adds a new child node before one of the existing child nodes
     *
     * @param existingChild the existing child node
     * @param newChild the new node to be added
     */
    void addBefore(TOCNode existingChild, TOCNode newChild);

    /**
     * Adds a new child node to the end of the child node list
     *
     * @param child the node to be added
     */
    void addToEnd(TOCNode child);
    
    /**
     * Removes a node from the whole subtree represented by this node
     *
     * @param child the child node to be removed
     */
    void deepRemove(TOCNode child);

    /**
     * Find the child node in the subtree represented by this node which refers
     * to the given page.
     *
     * @param page the page to look for
     * @return returns the node which refers to the page and belongs to the
     * subtree represented by this node, or null if there is no such node.
     */
    TOCNode findReferenceTo(Page page);

    /**
     * Checks if the page or any of its children refers to the given page
     *
     * @param page the page to look for
     * @return true if this node or any child node (recursively) refers to the
     * page
     */
    boolean isReferenced(Page page);

    /**
     * Removes one of the child nodes
     *
     * @param child the child node to be removed
     */
    void remove(TOCNode child);

    /**
     * Removes the node that refers to the given page from the whole subtree
     * represented by this node.
     *
     * @param page the page to look for
     */
    void removeReferenceTo(Page page);

    /**
     * Replaces this node to a new instance
     *
     * Useful for converting one node to another type
     * @param newNode The new node
     */
    void replace(TOCNode newNode);
    
    /**
     * Replaces one of the direct child nodes to another instance
     * @param child child to be replaced
     * @param newChild replacement node
     */    
    void replaceChild(TOCNode child, TOCNode newChild);
}
