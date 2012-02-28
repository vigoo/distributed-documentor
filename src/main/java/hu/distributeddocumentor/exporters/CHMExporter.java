package hu.distributeddocumentor.exporters;

import com.google.common.io.Files;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOC;
import hu.distributeddocumentor.model.TOCNode;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;

public class CHMExporter extends HTMLBasedExporter implements Exporter {
    
    private final DocumentorPreferences prefs;    
    private final Documentation doc;    
    private final Set<String> contentFiles = new HashSet<String>();

    public CHMExporter(DocumentorPreferences prefs, Documentation doc, File targetDir) {
        
        super(targetDir);
        
        this.prefs = prefs;
        this.doc = doc;        
    }

    @Override
    public void export() throws FileNotFoundException, IOException {
        
        // Creating the target directory if necessary
        if (!targetDir.exists())
            targetDir.mkdirs();
        
        // Exporting the pages
        TOC toc = doc.getTOC();
        for (TOCNode node : toc.getRoot().getChildren()) {
            if (node != toc.getRecycleBin()) {                
                exportReferencedPages(node);
            }
        }
        
        // Exporting the images
        File mediaDir = new File(targetDir, "media");
        if (!mediaDir.exists())
            mediaDir.mkdir();
        
        for (String image : doc.getImages().getImages()) {
            Files.copy(new File(doc.getImages().getMediaRoot(), image), 
                        new File(mediaDir, image));

            contentFiles.add(new File("media", image).getPath());
        }
        
        // Creating the contents (HHC) file
        createHHC(new File(targetDir, "toc.hhc"), toc);

        // Creating HHP file
        createHHP();
        
        // Executing HTML help compiler
        if (prefs.hasValidCHMCompilerPath()) {        
            String[] args = {prefs.getCHMCompilerPath(), "project.hhp"};
            Runtime.getRuntime().exec(args);
        } else {        
            JOptionPane.showMessageDialog(null, "The CHM compiler's path is not specified. Use the preferences dialog to set it!", "Export failed", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    @Override
    protected File exportPage(Page page) throws FileNotFoundException {
        File target = super.exportPage(page);
        
        contentFiles.add(target.getName());
        
        return target;
    }

    private void createHHP() throws FileNotFoundException {
        
        File hhp = new File(targetDir, "project.hhp");
        PrintWriter out = new PrintWriter(hhp);
        
        // http://chmspec.nongnu.org/latest/INI.html#HHP
        
        try {
            out.println("[OPTIONS]");
            //out.println("Binary TOC=No");
            //out.println("Binary Index=No");
            out.println("Compiled file=project.chm"); // TODO
            out.println("Contents file=toc.hhc");
            out.println("Index file=");
            //out.println("AutoIndex=No");
            //out.println("DefaultWindow=DefWin");
            out.println("Default topic=start.html");
            //out.println("Default Font=");
            out.println("Language=0x409 English (US)"); // TODO
            out.println("Title=Distributed Documentor generated CHM"); // TODO
            //out.println("CreateCHIFile=No");
            //out.println("Compatibility=1.1");
            out.println("Error log file=compiler.log");
            out.println("Full-text search=Yes");
            out.println("Display compile progress=Yes");
            out.println("Display compile note=Yes");
            out.println("Flat=No");
            //out.println("Full text search stop list file=");
            out.println();
            
            //out.println("[WINDOWS]");
            //out.println("DefWin=\"Distributed Documentor generated CHM\",\"toc.hhc\",,,\"start.html\",,,,,,0x2120,200,0x3006,[500,0,1050,400],,,,,,,0");
            //out.println();
            
            out.println("[FILES]");
            for (String s : contentFiles)
                out.println(s);
        }
        finally {
            out.close();
        }
    }

    private void createHHC(File file, TOC toc) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(file);
        
        // http://www.nongnu.org/chmspec/latest/Sitemap.html
        
        try {
            out.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
            out.println("<HTML>");
            out.println("<OBJECT type=\"text/site properties\">");
            out.println("  <param name=\"SiteType\" value=\"toc\">");
            out.println("</OBJECT>");
            out.println("<UL>");
            
            for (TOCNode node : toc.getRoot().getChildren()) {
                if (node != toc.getRecycleBin() &&
                    ((node != toc.getUnorganized()) ||
                     (node == toc.getUnorganized() && node.getChildren().size() > 0))) {
                    exportTOCNode(node, out, 6);
                }
            }
            
            out.println("</UL>");
            out.println("</HTML>");
        }
        finally {
            out.close();
        }
    }

    private void exportTOCNode(TOCNode node, PrintWriter out, int indent) {        
        String i = "";
        for (int j = 0; j < indent; j++)
            i += " ";
        
        out.println(i + "<LI><OBJECT type=\"text/sitemap\">");
        out.println(i + "    <param name=\"Name\" value=\"" + node.getTitle() + "\">");
        
        if (node.getTarget() != null)
            out.println(i + "    <param name=\"Local\" value=\"" + node.getTarget().getId() + ".html\">");
        out.println(i + "</OBJECT>");                
        
        if (node.getChildren().size() > 0) {
            
            out.println(i + "<UL>");
            
            for (TOCNode child : node.getChildren()) {
                exportTOCNode(child, out, indent+2);
            }
            
            out.println(i + "</UL>");
        }
    }
    
}
