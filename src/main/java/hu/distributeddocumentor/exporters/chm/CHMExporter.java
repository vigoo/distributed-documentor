package hu.distributeddocumentor.exporters.chm;

import hu.distributeddocumentor.model.Language;
import com.google.common.io.Files;
import com.google.inject.Inject;
import hu.distributeddocumentor.exporters.Exporter;
import hu.distributeddocumentor.exporters.HTMLBasedExporter;
import hu.distributeddocumentor.gui.LongOperationRunner;
import hu.distributeddocumentor.gui.ProgressUI;
import hu.distributeddocumentor.gui.RunnableWithProgress;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.ExportableNode;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.toc.TOC;
import hu.distributeddocumentor.model.toc.TOCNode;
import hu.distributeddocumentor.model.virtual.builders.VirtualNodeException;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.utils.ResourceUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public class CHMExporter extends HTMLBasedExporter implements Exporter {
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CHMExporter.class);
    
    private final Set<String> contentFiles = new HashSet<>();
    private File targetDir;

    @Inject
    public CHMExporter(DocumentorPreferences prefs) {
        super(prefs);
    }

    @Override
    public void export(final Documentation doc, final File targetDir, LongOperationRunner longOp) throws FileNotFoundException, IOException, VirtualNodeException {
        
           try {
            longOp.run(new RunnableWithProgress() {

                @Override
                public void run(ProgressUI progress) {                        
                    try {
                        export(doc, targetDir, progress);
                    } catch (IOException|InterruptedException|VirtualNodeException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });     
        }
        catch (RuntimeException ex) {
            Throwable inner = findInnerException(ex);
            if (inner instanceof FileNotFoundException)
                throw (FileNotFoundException)inner;
            else if (inner instanceof IOException)
                throw (IOException)inner;
            else if (inner instanceof VirtualNodeException)
                throw (VirtualNodeException)inner;
            else
                throw ex;
        }
    }
     
    private void export(Documentation doc, File targetDir, ProgressUI progress) throws FileNotFoundException, IOException, InterruptedException, VirtualNodeException {
           
        this.targetDir = targetDir;
        
        progress.setStatus("Exporting files...");
        
        // Creating the target directory if necessary
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new RuntimeException("Failed to create target directory!");
            }
        }                   
        
        // Exporting the CSS file
        extractResource("/documentation.css", "documentation.css", targetDir);        
        
        if (doc.getCustomStylesheet().exists()) {
            Files.copy(doc.getCustomStylesheet(), new File(targetDir, "custom.css"));
        }
        
        // Exporting the syntax highlighter         
        File shDir = new File(targetDir, "syntaxhighlighter");
        if (!shDir.exists()) {
            if (!shDir.mkdir()) {
                throw new RuntimeException("Failed to create syntaxhighlighter directory!");
            }
        }
                
        extractResource("/syntaxhighlighter/shCore.css", "shCore.css", shDir);
        extractResource("/syntaxhighlighter/shThemeDefault.css", "shThemeDefault.css", shDir);
        extractResource("/syntaxhighlighter/shCore.js", "shCore.js", shDir);
        extractResource("/syntaxhighlighter/shBrushAS3.js", "shBrushAS3.js", shDir);
        extractResource("/syntaxhighlighter/shBrushAppleScript.js", "shBrushAppleScript.js", shDir);
        extractResource("/syntaxhighlighter/shBrushBash.js", "shBrushBash.js", shDir);
        extractResource("/syntaxhighlighter/shBrushCSharp.js", "shBrushCSharp.js", shDir);
        extractResource("/syntaxhighlighter/shBrushColdFusion.js", "shBrushColdFusion.js", shDir);
        extractResource("/syntaxhighlighter/shBrushCpp.js", "shBrushCpp.js", shDir);
        extractResource("/syntaxhighlighter/shBrushCss.js", "shBrushCss.js", shDir);
        extractResource("/syntaxhighlighter/shBrushDelphi.js", "shBrushDelphi.js", shDir);
        extractResource("/syntaxhighlighter/shBrushDiff.js", "shBrushDiff.js", shDir);
        extractResource("/syntaxhighlighter/shBrushErlang.js", "shBrushErlang.js", shDir);
        extractResource("/syntaxhighlighter/shBrushGroovy.js", "shBrushGroovy.js", shDir);
        extractResource("/syntaxhighlighter/shBrushJScript.js", "shBrushJScript.js", shDir);
        extractResource("/syntaxhighlighter/shBrushJava.js", "shBrushJava.js", shDir);
        extractResource("/syntaxhighlighter/shBrushJavaFX.js", "shBrushJavaFX.js", shDir);
        extractResource("/syntaxhighlighter/shBrushPerl.js", "shBrushPerl.js", shDir);
        extractResource("/syntaxhighlighter/shBrushPhp.js", "shBrushPhp.js", shDir);
        extractResource("/syntaxhighlighter/shBrushPlain.js", "shBrushPlain.js", shDir);
        extractResource("/syntaxhighlighter/shBrushPowerShell.js", "shBrushPowerShell.js", shDir);
        extractResource("/syntaxhighlighter/shBrushPython.js", "shBrushPython.js", shDir);
        extractResource("/syntaxhighlighter/shBrushRuby.js", "shBrushRuby.js", shDir);
        extractResource("/syntaxhighlighter/shBrushSass.js", "shBrushSass.js", shDir);
        extractResource("/syntaxhighlighter/shBrushScala.js", "shBrushScala.js", shDir);
        extractResource("/syntaxhighlighter/shBrushSql.js", "shBrushSql.js", shDir);
        extractResource("/syntaxhighlighter/shBrushVb.js", "shBrushVb.js", shDir);
        extractResource("/syntaxhighlighter/shBrushXml.js", "shBrushXml.js", shDir);
        
        progress.setStatus("Exporting pages...");
        
        // Exporting the pages
        final TOC toc = doc.getTOC();
        final File repoRoot = new File(doc.getRepositoryRoot());
        
        for (TOCNode node : toc.getRoot().getChildren()) {
            if (node != toc.getRecycleBin()) {                
                exportReferencedPages(repoRoot, targetDir, node);
            }
        }
        
        progress.setStatus("Exporting images...");
        
        // Exporting the images
        File mediaDir = new File(targetDir, "media");
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdir()) {
                throw new RuntimeException("Failed to create media directory!");
            }
        }
        
        int imageCount = doc.getImages().getImages().size();
        int i = 0;
        for (String image : doc.getImages().getImages()) {
            Files.copy(new File(doc.getImages().getMediaRoot(), image), 
                        new File(mediaDir, image));

            contentFiles.add(new File("media", image).getPath());
            progress.setProgress((double)(i++)/(double)imageCount);
        }
        
        progress.setStatus("Compiling CHM...");
        progress.setIndeterminate();
        
        // Creating the contents (HHC) file
        createHHC(new File(targetDir, "toc.hhc"), toc, doc.getLanguage());

        // Creating HHP file
        createHHP(doc, targetDir);
        
        // Executing HTML help compiler
        if (prefs.hasValidCHMCompilerPath()) {        
            String[] args = {prefs.getCHMCompilerPath(), "project.hhp"};
            runCompiler(args, targetDir);                       
        } else {        
            JOptionPane.showMessageDialog(null, "The CHM compiler's path is not specified. Use the preferences dialog to set it!", "Export failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void runCompiler(String[] args, File targetDir) throws IOException {
        Process compiler = new ProcessBuilder(args)
                .directory(targetDir)
                .redirectErrorStream(true)
                .start();
        
        InputStream stdout = compiler.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
        
        String line;
        while ((line = reader.readLine()) != null) {
            log.debug(line.trim());
        }
    }

    @Override
    protected void extractResource(String resourceName, String fileName, File targetDir) throws IOException {
        super.extractResource(resourceName, fileName, targetDir);
        
        contentFiles.add(resourceName.substring(1).replace('/', '\\'));
    }
            
    @Override
    protected File exportPage(Page page, File targetDir) throws FileNotFoundException {
        File target = super.exportPage(page, targetDir);
                        
        contentFiles.add(ResourceUtils.getRelativePath(target.getAbsolutePath(), this.targetDir.getAbsolutePath()));
        
        return target;
    }

    private void createHHP(Documentation doc, File targetDir) throws FileNotFoundException {
        
        File hhp = new File(targetDir, "project.hhp");
        
        // http://chmspec.nongnu.org/latest/INI.html#HHP
        
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(hhp), Charset.forName(charsetForLanguage(doc.getLanguage()))))){
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
            switch (doc.getLanguage()) {
                case ENGLISH:
                    out.println("Language=0x409 English (US)");
                case JAPANESE:
                    out.println("Language=0x411 Japanese (Japan)");
            }
            
            out.println("Title="+doc.getTitle());
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
    
    private String charsetForLanguage(Language language) {
        String charset = CHARSET;
        switch (language) {
            case ENGLISH: 
                charset = CHARSET;
                break;
            case JAPANESE:
                charset = "Shift-JIS";
                break;
        }
        return charset;
    }

    private void createHHC(File file, TOC toc, Language language) throws FileNotFoundException {
        
        // http://www.nongnu.org/chmspec/latest/Sitemap.html
        
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), Charset.forName(charsetForLanguage(language))))) {
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
