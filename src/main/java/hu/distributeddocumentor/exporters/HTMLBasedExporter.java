package hu.distributeddocumentor.exporters;

import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOCNode;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class HTMLBasedExporter {
    
    protected final Map<TOCNode, TOCNode> realNodes;                    

    public HTMLBasedExporter() {       
        realNodes = new HashMap<>();
    }        

    protected void exportReferencedPages(File repositoryRoot, File targetDir, TOCNode node) throws FileNotFoundException {
        
        TOCNode realNode = node.getRealNode(repositoryRoot);        
        realNodes.put(node, realNode);
        
        Page page = realNode.getTarget();
        if (page != null) {
            exportPage(page, targetDir);
        }
        
        for (TOCNode childNode : realNode.getChildren()) {
            exportReferencedPages(repositoryRoot, targetDir, childNode);
        }
    }

    protected File exportPage(Page page, File targetDir) throws FileNotFoundException {        
        
        File target = new File(targetDir, page.getId()+".html");
        String html = page.asHTML();
        
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
}
