package hu.distributeddocumentor.exporters.chm;

import com.google.common.io.Files;
import com.google.inject.Inject;
import hu.distributeddocumentor.exporters.Exporter;
import hu.distributeddocumentor.exporters.HTMLBasedExporter;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.ExportableNode;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOC;
import hu.distributeddocumentor.model.TOCNode;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.utils.ResourceUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;

public class CHMExporter extends HTMLBasedExporter implements Exporter {
    private final Set<String> contentFiles = new HashSet<>();
    private File targetDir;

    @Inject
    public CHMExporter(DocumentorPreferences prefs) {
        super(prefs);
    }

    @Override
    public void export(Documentation doc, File targetDir) throws FileNotFoundException, IOException {
        
        this.targetDir = targetDir;
        
        // Creating the target directory if necessary
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new RuntimeException("Failed to create target directory!");
            }
        }                   
        
        // Exporting the CSS file
        extractResource("/documentation.css", "documentation.css", targetDir);
        contentFiles.add("documentation.css");
        
        // Exporting the pages
        final TOC toc = doc.getTOC();
        final File repoRoot = new File(doc.getRepositoryRoot());
        
        for (TOCNode node : toc.getRoot().getChildren()) {
            if (node != toc.getRecycleBin()) {                
                exportReferencedPages(repoRoot, targetDir, node);
            }
        }
        
        // Exporting the images
        File mediaDir = new File(targetDir, "media");
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdir()) {
                throw new RuntimeException("Failed to create media directory!");
            }
        }
        
        for (String image : doc.getImages().getImages()) {
            Files.copy(new File(doc.getImages().getMediaRoot(), image), 
                        new File(mediaDir, image));

            contentFiles.add(new File("media", image).getPath());
        }
        
        // Creating the contents (HHC) file
        createHHC(new File(targetDir, "toc.hhc"), toc);

        // Creating HHP file
        createHHP(targetDir);
        
        // Executing HTML help compiler
        if (prefs.hasValidCHMCompilerPath()) {        
            String[] args = {prefs.getCHMCompilerPath(), "project.hhp"};
            Runtime.getRuntime().exec(args, null, targetDir);
        } else {        
            JOptionPane.showMessageDialog(null, "The CHM compiler's path is not specified. Use the preferences dialog to set it!", "Export failed", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    @Override
    protected File exportPage(Page page, File targetDir) throws FileNotFoundException {
        File target = super.exportPage(page, targetDir);
                        
        contentFiles.add(ResourceUtils.getRelativePath(target.getAbsolutePath(), this.targetDir.getAbsolutePath()));
        
        return target;
    }

    private void createHHP(File targetDir) throws FileNotFoundException {
        
        File hhp = new File(targetDir, "project.hhp");
        
        // http://chmspec.nongnu.org/latest/INI.html#HHP
        
        try (PrintWriter out = new PrintWriter(hhp)) {
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
            for (String s : contentFiles) {
                out.println(s);
            }
        }
    }

    private void createHHC(File file, TOC toc) throws FileNotFoundException {
        
        // http://www.nongnu.org/chmspec/latest/Sitemap.html
        
        try (PrintWriter out = new PrintWriter(file)) {
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
                    exportTOCNode(node, out, 6, "");
                }
            }
            
            out.println("</UL>");
            out.println("</HTML>");
        }
    }

    private void exportTOCNode(TOCNode node, PrintWriter out, int indent, String scope) {        
        final ExportableNode exportable = realNodes.get(node);
        final TOCNode realNode = exportable.getNode();
                
        if (exportable.getScope() != null) {
            scope = scope + exportable.getScope() + "/";
        }
        
        String i = StringUtils.repeat(" ", indent);
        
        out.println(i + "<LI><OBJECT type=\"text/sitemap\">");
        out.println(i + "    <param name=\"Name\" value=\"" + realNode.getTitle() + "\">");
        
        if (realNode.getTarget() != null) {
            out.println(i + "    <param name=\"Local\" value=\"" + scope + realNode.getTarget().getId() + ".html\">");
        }
        out.println(i + "</OBJECT>");                
        
        if (realNode.getChildren().size() > 0) {
            
            out.println(i + "<UL>");
            
            for (TOCNode child : realNode.getChildren()) {
                exportTOCNode(child, out, indent+2, scope);
            }
            
            out.println(i + "</UL>");
        }
    }

    @Override
    public String getTargetName() {
        return "CHM";
    }
    
    @Override
    public String toString() {
        return getTargetName();
    }

    @Override
    protected File getTargetRootDir() {
        return targetDir;
    }

    @Override
    protected void exportExtraImages(Set<File> extraImages, File targetDir) throws IOException {
        // Exporting the images
        File mediaDir = new File(targetDir, "media");
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdir()) {
                throw new RuntimeException("Failed to create media directory!");
            }
        }
        
        for (File image : extraImages) {            
            final File targetFile = new File(mediaDir, image.getName());            
            Files.copy(image, targetFile);            
            contentFiles.add(ResourceUtils.getRelativePath(targetFile.getAbsolutePath(), this.targetDir.getAbsolutePath()));
        }
    }
}
