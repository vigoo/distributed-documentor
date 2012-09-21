package hu.distributeddocumentor.model.virtual;

import hu.distributeddocumentor.model.TOCNode;

/**
 * Represents a virtual documentation hierarchy builder
 * 
 * <p>
 * A virtual hierarchy builder emits a root node {@link TOCNode}
 * representing a subtree which can refer to any number of generated
 * pages in any supported markup.
 * 
 * <p>
 * The generated nodes and their pages cannot be edited by the user. 
 * The builder is invoked only when the documentation is being exported.
 * 
 * @author Daniel Vigovszky
 */
public interface VirtualHierarchyBuilder {

    /**
     * Build the virtual documentation hierarchy
     * 
     * @return the root node of the generated subtree
     */
    TOCNode build();
}
