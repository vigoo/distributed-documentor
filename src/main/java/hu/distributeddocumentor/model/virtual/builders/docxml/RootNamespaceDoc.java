package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RootNamespaceDoc extends NamespaceDoc {
    
    private final static Logger log = LoggerFactory.getLogger(RootNamespaceDoc.class);
    
    private String assemblyName;

    public String getAssemblyName() {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }
        

    public RootNamespaceDoc(String pageId, Function<String, String> idGenerator) {
        super(null, "", pageId, idGenerator);                
    }

    @Override
    public String getAsPrefix() {
        return "";
    }    
                        
    @Override
    public void renderPage(final WikiWriter writer) {
        
        try {
            writer.heading(1, assemblyName); 
            writer.newParagraph();
            writer.text("This is the root of the assembly's reference manual.\n");
            writer.text("The manual is structured by ");
            writer.italic("namespaces");
            writer.text(" containing ");
            writer.italic("classes");
            writer.text(". There is one documentation page for each class, describing\n");
            writer.text("all the fields, properties, events and methods of the class.");
            writer.newParagraph();
            
            for (NamespaceDoc ns : getSortedNamespaces()) {
                ns.writeBullet(writer, 1);
            }
            writer.newParagraph();
        }
        catch (IOException ex) {
            log.error("Failed to render root page because of: " + ex.getMessage());
            // TODO: write to output as well
        }
    }
}
