package hu.distributeddocumentor.exporters;

import hu.distributeddocumentor.model.ExportableNode;
import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOCNode;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.utils.ResourceUtils;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public abstract class HTMLBasedExporter {
    
    protected final Map<TOCNode, ExportableNode> realNodes;                    
    protected final DocumentorPreferences prefs;

    public HTMLBasedExporter(DocumentorPreferences prefs) {       
        this.prefs = prefs;
        realNodes = new HashMap<>();
    }        

    protected void exportReferencedPages(File repositoryRoot, File targetDir, TOCNode node) throws FileNotFoundException {
        
        final ExportableNode exportable = node.getRealNode(repositoryRoot, prefs);        
        final TOCNode realNode = exportable.getNode();
        
        realNodes.put(node, exportable);
        
        final Page page = realNode.getTarget();
        final File newTargetDir;        
        if (exportable.getScope() != null) {
            newTargetDir = new File(targetDir, exportable.getScope());
            
            if (!newTargetDir.exists()) {
                if (!newTargetDir.mkdirs()) {
                    throw new RuntimeException("Failed to create target subdirectory!");
                }
            }
        }
        else {
            newTargetDir = targetDir;
        }
        
        if (page != null) {
            exportPage(page, newTargetDir);
        }
        
        for (TOCNode childNode : realNode.getChildren()) {
            exportReferencedPages(repositoryRoot, newTargetDir, childNode);
        }
    }

    protected File exportPage(Page page, File targetDir) throws FileNotFoundException {        
        
        File target = new File(targetDir, page.getId()+".html");
        String html = page.asHTML(ResourceUtils.getRelativePath(getTargetRootDir().getAbsolutePath(), targetDir.getAbsolutePath())+"/");
        
        try (PrintWriter out = new PrintWriter(target)) {
            out.print(html);
        }                
        
        return target;
    }
    
    protected final void extractResource(final String resourceName,
                                         final String fileName,
                                         final File targetDir)
            throws IOException {
       
        try (InputStream input = HTMLBasedExporter.class.getResourceAsStream(resourceName)) {
            extract(input, fileName, targetDir);
        }       
    }
        
    protected void extract(InputStream stream, String fileName, File targetDir) throws IOException {
        
        byte[] buf = new byte[16384];
        int read;
        
        File target = new File(targetDir, fileName);
        
        try (FileOutputStream out = new FileOutputStream(target)) {
            while ((read = stream.read(buf)) != -1) {

                out.write(buf, 0, read);
            }        
        }
    }

    protected abstract File getTargetRootDir();        
}
