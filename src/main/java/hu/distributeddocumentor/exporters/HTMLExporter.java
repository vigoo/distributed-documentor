package hu.distributeddocumentor.exporters;

import com.google.common.io.Files;
import hu.distributeddocumentor.model.Documentation;
import hu.distributeddocumentor.model.TOC;
import hu.distributeddocumentor.model.TOCNode;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


public class HTMLExporter extends HTMLBasedExporter implements Exporter {

    private final DocumentorPreferences prefs;    
    private final Documentation doc;

    public HTMLExporter(DocumentorPreferences prefs, Documentation doc, File targetDir) {
        
        super(targetDir);
        
        this.prefs = prefs;
        this.doc = doc;
    }
    
    
    @Override
    public void export() throws FileNotFoundException, IOException {
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
        }
        
        // Creating the TOC frame
        createTreeItemsJS(new File(targetDir, "tree_items.js"), toc);
        
        // Extracting static files (icons, scripts, etc)
        extract(getClass().getResourceAsStream("/documentation.css"), "documentation.css", targetDir);
        extract(getClass().getResourceAsStream("/tree/tree.html"), "tree.html", targetDir);
        extract(getClass().getResourceAsStream("/tree/index.html"), "index.html", targetDir);
        
        extract(getClass().getResourceAsStream("/tree/tree.js"), "tree.js", targetDir);
        extract(getClass().getResourceAsStream("/tree/tree_tpl.js"), "tree_tpl.js", targetDir);
        
        File iconsDir = new File(targetDir, "icons");
        if (!iconsDir.exists())
            iconsDir.mkdir();
        
        extract(getClass().getResourceAsStream("/tree/icons/empty.gif"), "empty.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/folder.gif"), "folder.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/folderopen.gif"), "folderopen.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/foldersel.gif"), "foldersel.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/minus.gif"), "minus.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/page.gif"), "page.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/pagesel.gif"), "pagesel.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/plus.gif"), "plus.gif", iconsDir);        
        extract(getClass().getResourceAsStream("/tree/icons/base.gif"), "base.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/join.gif"), "join.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/joinbottom.gif"), "joinbottom.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/line.gif"), "line.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/minusbottom.gif"), "minusbottom.gif", iconsDir);
        extract(getClass().getResourceAsStream("/tree/icons/plusbottom.gif"), "plusbottom.gif", iconsDir);        
    }

    private void createTreeItemsJS(File file, TOC toc) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(file);
        
        try {
            out.println("var TREE_ITEMS = [");
                        
            for (TOCNode node : toc.getRoot().getChildren()) {
                if (node != toc.getRecycleBin() &&
                    ((node != toc.getUnorganized()) ||
                     (node == toc.getUnorganized() && node.getChildren().size() > 0))) {
                    exportTOCNode(node, out, 6, node == toc.getRoot().getChildren().get(0));
                }
            }
            
            out.println();
            out.println("];");
        }
        finally {
            out.close();
        }
    }

    private void exportTOCNode(TOCNode node, PrintWriter out, int indent, boolean isFirst) {
        String i = "";
        for (int j = 0; j < indent; j++)
            i += " ";
        
        if (isFirst)
            out.println();
        else
            out.println(",");

        out.print(i+"['"+fixTitle(node.getTitle())+"'");
        if (node.hasTarget()) {            
            out.print(", '"+node.getTarget().getId()+".html'");
        } else {
            out.print(", null");
        }
        
        if (node.getChildren().size() > 0) {
            
            out.println(",");
            
            for (int j = 0; j < node.getChildren().size(); j++) {
                
                TOCNode child = node.getChildren().get(j);
                exportTOCNode(child, out, indent + 4, j == 0);
            }
            
            out.println();
            out.print(i);
        }
                
        out.print("]");     
    }

    private String fixTitle(String title) {
        return title.replaceAll("'", "\\'");
    }
}
