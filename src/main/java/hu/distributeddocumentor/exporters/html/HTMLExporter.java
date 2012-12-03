package hu.distributeddocumentor.exporters.html;

import com.google.common.io.Files;
import com.google.inject.Inject;
import hu.distributeddocumentor.exporters.Exporter;
import hu.distributeddocumentor.exporters.HTMLBasedExporter;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.ExportableNode;
import hu.distributeddocumentor.model.toc.TOC;
import hu.distributeddocumentor.model.toc.TOCNode;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class HTMLExporter extends HTMLBasedExporter implements Exporter {
    private File targetDir;

    @Inject
    public HTMLExporter(DocumentorPreferences prefs) {        
        super(prefs);
    }
    
    
    @Override
    public void export(Documentation doc, File targetDir) throws FileNotFoundException, IOException {
        
        this.targetDir = targetDir;
        File repositoryRoot = new File(doc.getRepositoryRoot());
        
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new RuntimeException("Failed to create target directory!");
            }
        }
        
        // Exporting the pages
        final TOC toc = doc.getTOC();
        
        for (TOCNode node : toc.getRoot().getChildren()) {
            if (node != toc.getRecycleBin()) {
                exportReferencedPages(repositoryRoot, targetDir, node);
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
        }
        
        // Creating the TOC frame
        createTreeItemsJS(new File(targetDir, "tree_items.js"), toc);
        
        // Extracting static files (icons, scripts, etc)
        extractResource("/documentation.css", "documentation.css", targetDir);
        extractResource("/tree/tree.html", "tree.html", targetDir);
        extractResource("/tree/index.html", "index.html", targetDir);
        
        extractResource("/tree/tree.js", "tree.js", targetDir);
        extractResource("/tree/tree_tpl.js", "tree_tpl.js", targetDir);
        
        File iconsDir = new File(targetDir, "icons");
        if (!iconsDir.exists()) {
            if (!iconsDir.mkdir()) {
                throw new RuntimeException("Failed to create icons directory!");
            }
        }
        
        extractResource("/tree/icons/empty.gif", "empty.gif", iconsDir);
        extractResource("/tree/icons/folder.gif", "folder.gif", iconsDir);
        extractResource("/tree/icons/folderopen.gif", "folderopen.gif", iconsDir);
        extractResource("/tree/icons/foldersel.gif", "foldersel.gif", iconsDir);
        extractResource("/tree/icons/minus.gif", "minus.gif", iconsDir);
        extractResource("/tree/icons/page.gif", "page.gif", iconsDir);
        extractResource("/tree/icons/pagesel.gif", "pagesel.gif", iconsDir);
        extractResource("/tree/icons/plus.gif", "plus.gif", iconsDir);        
        extractResource("/tree/icons/base.gif", "base.gif", iconsDir);
        extractResource("/tree/icons/join.gif", "join.gif", iconsDir);
        extractResource("/tree/icons/joinbottom.gif", "joinbottom.gif", iconsDir);
        extractResource("/tree/icons/line.gif", "line.gif", iconsDir);
        extractResource("/tree/icons/minusbottom.gif", "minusbottom.gif", iconsDir);
        extractResource("/tree/icons/plusbottom.gif", "plusbottom.gif", iconsDir);        
        
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

        
    }

    private void createTreeItemsJS(File file, TOC toc) throws FileNotFoundException {
        
        try (PrintWriter out = new PrintWriter(file)) {
            out.println("var TREE_ITEMS = [");
                        
            for (TOCNode node : toc.getRoot().getChildren()) {
                if (node != toc.getRecycleBin() &&
                    ((node != toc.getUnorganized()) ||
                     (node == toc.getUnorganized() && node.getChildren().size() > 0))) {
                    exportTOCNode(node, out, 6, "", node == toc.getRoot().getChildren().get(0));
                }
            }
            
            out.println();
            out.println("];");
        }
    }

    private void exportTOCNode(TOCNode node, PrintWriter out, int indent, String scope, boolean isFirst) {
        
        final ExportableNode exportable = realNodes.get(node);
        final TOCNode realNode = exportable.getNode();
        
        if (exportable.getScope() != null) {
            scope = scope + exportable.getScope() + "/";
        }
                       
        String i = StringUtils.repeat(" ", indent);
        
        if (isFirst) {
            out.println();
        }
        else {
            out.println(",");
        }

        out.print(i+"['"+fixTitle(realNode.getTitle())+"'");
        if (realNode.hasTarget()) {            
            out.print(", '"+scope+realNode.getTarget().getId()+".html'");
        } else {
            out.print(", null");
        }
        
        if (realNode.getChildren().size() > 0) {
            
            out.println(",");
            
            for (int j = 0; j < realNode.getChildren().size(); j++) {
                
                TOCNode child = realNode.getChildren().get(j);
                exportTOCNode(child, out, indent + 4, scope, j == 0);
            }
            
            out.println();
            out.print(i);
        }
                
        out.print("]");     
    }

    private String fixTitle(String title) {
        return StringUtils.replace(title, "'", "\\'");
    }

    @Override
    public String getTargetName() {
        return "HTML";
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
            Files.copy(image, new File(mediaDir, image.getName()));            
        }
    }
}
