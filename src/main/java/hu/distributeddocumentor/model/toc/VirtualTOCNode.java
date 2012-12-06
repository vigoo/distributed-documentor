
package hu.distributeddocumentor.model.toc;


public interface VirtualTOCNode extends TOCNode {

    /**
     * Gets the source path passed for the hierarchy builder if this is a virtual
     * root node.
     *
     * @return the source path, or null if this is no a virtual root node
     */
    String getSourcePath();

    /**
     * Gets the type of the virtual hierarchy builder implementation to be used
     * to generate the child nodes of this virtual root node.
     *
     * @return the type of the builder or null if this is no a virtual root node
     */
    Class getVirtualHierarchyBuilder();

    /**
     * Sets the source path to be passed to the hierarchy builder when this node
     * is treated as a virtual root node.
     *
     * @param sourcePath the source path to be passed
     */
    void setSourcePath(String sourcePath);

    /**
     * Sets the type of the virtual hierarchy builder implementation to be used
     * to generate the child nodes of this virtual root node.
     *
     * @param virtualHierarchyBuilder the type of the builder class implementing
     *                                {@link VirtualHierarchyBuilder}
     */
    void setVirtualHierarchyBuilder(Class virtualHierarchyBuilder);
    
}
