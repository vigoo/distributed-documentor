package hu.distributeddocumentor.model;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Wraps an TOCNode and some additional export related information
 * @author Daniel Vigovszky
 */
public class ExportableNode {
    private final TOCNode node;
    private final String scope;
    private final Set<File> extraImages;

    /**
     * Gets the real (non-virtual) TOC node     
     */
    public TOCNode getNode() {
        return node;
    }

    /**
     * Gets the optional scope of the subtree represented by the node
     * @return Returns the scope or null if it should not be changed
     */
    public String getScope() {
        return scope;
    }   

    /**
     * Gets the extra image files to be included in the exported documentation     
     */
    public Set<File> getExtraImages() {
        return extraImages;
    }        
    
    /**
     * Creates an exportable node object
     * @param node The real TOC node
     * @param scope Optional scope change, use null if not used
     * @param extraImages Optionally empty collection of image files to be included 
     *                    in the 
     */
    public ExportableNode(TOCNode node, String scope, Collection<File> extraImages) {
        this.node = node;
        this.scope = scope;
        this.extraImages = new HashSet<>(extraImages);
    }        
}
