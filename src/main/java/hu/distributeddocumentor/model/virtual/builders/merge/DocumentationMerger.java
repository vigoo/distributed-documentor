package hu.distributeddocumentor.model.virtual.builders.merge;

import hu.distributeddocumentor.gui.LongOperationRunner;
import hu.distributeddocumentor.gui.SimpleLongOperationRunner;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.FailedToLoadMetadataException;
import hu.distributeddocumentor.model.FailedToLoadPageException;
import hu.distributeddocumentor.model.FailedToLoadTOCException;
import hu.distributeddocumentor.model.builders.UsesPreferences;
import hu.distributeddocumentor.model.toc.TOCNode;
import hu.distributeddocumentor.model.toc.TOCNodeFactory;
import hu.distributeddocumentor.model.virtual.VirtualHierarchyBuilder;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This virtual hierarchy builder implementation imports an other full 
 * documentation to a subtree of the current one.
 * 
 * @author Daniel Vigovszky
 */
public class DocumentationMerger implements VirtualHierarchyBuilder, UsesPreferences {
    private final File innerDocumentationRoot;
    private final String title;
    private final TOCNodeFactory factory;
    private DocumentorPreferences prefs;
    private Documentation doc;
    private final LongOperationRunner longOp;
   
    public DocumentationMerger(File innerDocumentationRoot, String title, String markupLanguage, TOCNodeFactory factory) {
        this.innerDocumentationRoot = innerDocumentationRoot;
        this.title = title;
        this.factory = factory;
        this.longOp = new SimpleLongOperationRunner();
        
    }
    
    @Override
    public TOCNode build() {
        try {
            ensureDocumentLoaded();            
            
            final TOCNode root = doc.getTOC().getRoot();
            final TOCNode result = factory.createNode(title);
            
            for (final TOCNode child : root.getChildren()) {
                if ((child != doc.getTOC().getRecycleBin()) &&
                    ((child != doc.getTOC().getUnorganized()) || (doc.getTOC().getUnorganized().getChildren().size() > 0))) {
                    factory.getOperations(result).addToEnd(child);
                }
            }
            
            return result;
        } catch (FailedToLoadPageException | FailedToLoadTOCException | FailedToLoadMetadataException ex) {
            Logger.getLogger(DocumentationMerger.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void setPreferences(DocumentorPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public String getScope() {
        return innerDocumentationRoot.getName();
    }

    @Override
    public Collection<File> getExtraImages() {
        
        HashSet<File> files = new HashSet<>();
        try {
            ensureDocumentLoaded();
            
            File root = doc.getImages().getMediaRoot();
            for (String imageName : doc.getImages().getImages()) {
                files.add(new File(root, imageName));
            }
            
        } catch (FailedToLoadPageException | FailedToLoadTOCException | FailedToLoadMetadataException ex) {
            Logger.getLogger(DocumentationMerger.class.getName()).log(Level.SEVERE, null, ex);            
        }        
        
        return files;
    }

    private void ensureDocumentLoaded() throws FailedToLoadPageException, FailedToLoadTOCException, FailedToLoadMetadataException {
        if (doc == null) {
            doc = new Documentation(prefs);
            doc.initFromExisting(innerDocumentationRoot, longOp);
        }
    }
    
}
