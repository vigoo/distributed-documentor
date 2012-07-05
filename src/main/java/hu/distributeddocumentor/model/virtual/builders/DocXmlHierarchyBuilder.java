package hu.distributeddocumentor.model.virtual.builders;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOCNode;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.File;
import java.io.IOException;


public class DocXmlHierarchyBuilder extends VirtualHierarchyBuilderBase {
    private final File xmlFile;    
    private final String title;
    
    private final String rootId;

    public DocXmlHierarchyBuilder(File xmlFile, String title, String markupLanguage) {
        super(markupLanguage);
        
        this.xmlFile = xmlFile;
        this.title = title;
        
        rootId = generateId();
    }   
    
    @Override
    public TOCNode build() {
        
        Page rootPage = virtualPage(rootId, 
                new Function<WikiWriter, Void>() {

                    @Override
                    public Void apply(WikiWriter writer) {
                        renderRootPage(writer);
                        return null;
                    }});
        
        TOCNode root = createNode(title, rootPage);
        // TODO: add flat namespace nodes
        
        return root;
           
    }
                
    private void renderRootPage(WikiWriter writer) {
        
        try {
            writer.heading(1, "assembly name"); // TODO
            writer.newParagraph();
            writer.text("This is the root of the assembly's reference manual.\n");
            writer.text("The manual is structured by ");
            writer.italic("namespaces");
            writer.text(" containing ");
            writer.italic("classes");
            writer.text(". There is one documentation page for each class, describing\n");
            writer.text("all the fields, properties, events and methods of the class.");
            writer.newParagraph();
            
            // TODO: write bulleted namespace list with links
        }
        catch (IOException ex) {
            // TODO: write exception to log and generated page
        }
    }

}
