package hu.distributeddocumentor.exporters;

import hu.distributeddocumentor.model.Page;
import hu.distributeddocumentor.model.TOCNode;
import java.io.*;


public class HTMLBasedExporter {
    
    protected final File targetDir;

    public HTMLBasedExporter(File targetDir) {
        this.targetDir = targetDir;
    }        

    protected void exportReferencedPages(TOCNode node) throws FileNotFoundException {
        
        Page page = node.getTarget();
        if (page != null) {
            exportPage(page);
        }
        
        for (TOCNode childNode : node.getChildren())
            exportReferencedPages(childNode);
    }

    protected File exportPage(Page page) throws FileNotFoundException {        
        
        File target = new File(targetDir, page.getId()+".html");
        String html = page.asHTML();
        
        PrintWriter out = new PrintWriter(target);
        out.print(html);
        out.close();                
        
        return target;
    }
    
    protected final void extractResource(final String resourceName,
                                         final String fileName,
                                         final File targetDir)
            throws IOException {

        final InputStream input = HTMLBasedExporter.class.getResourceAsStream(resourceName);
        try {
            extract(input, fileName, targetDir);
        } finally {
            input.close();
        }       
    }
        
    protected void extract(InputStream stream, String fileName, File targetDir) throws IOException {
        
        byte[] buf = new byte[16384];
        int read;
        
        File target = new File(targetDir, fileName);
        FileOutputStream out = new FileOutputStream(target);
        
        try {
            while ((read = stream.read(buf)) != -1) {

                out.write(buf, 0, read);
            }        
        }
        finally {
            out.close();
        }
    }
}
