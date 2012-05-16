package hu.distributeddocumentor.model.builders;

import java.io.File;
import java.io.Writer;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.util.XmlStreamWriter;

public class LinkFixingBuilder extends HtmlDocumentBuilder {
    private final File root;

    public LinkFixingBuilder(XmlStreamWriter writer, File root) {
        super(writer);
        this.root = root;
    }

    public LinkFixingBuilder(Writer out, boolean formatting, File root) {
        super(out, formatting);
        this.root = root;
    }

    public LinkFixingBuilder(Writer out, File root) {
        super(out);
        this.root = root;
    }
        
    @Override
    public void image(Attributes attributes, String url) {
        
        if (!url.startsWith("http://") && 
            !url.startsWith("https://") &&
            !url.startsWith("file://") &&
            !url.startsWith("media/"))
            url = "media/"+url;
        
        if (root != null)
            url = root.toURI().toString() + url;
        
        super.image(attributes, url);
    }

    @Override
    public void link(Attributes attributes, String hrefOrHashName, String text) {
        
        if (hrefOrHashName.startsWith("http://") ||
            hrefOrHashName.startsWith("https://") ||
            hrefOrHashName.startsWith("file://") ||
            hrefOrHashName.endsWith(".html") ||
            hrefOrHashName.startsWith("#")) {
                
            super.link(attributes, hrefOrHashName, text);
        } else {
            
            if (hrefOrHashName.contains("#")) {            
                
                String[] parts = hrefOrHashName.split("#");
                super.link(attributes, parts[0]+".html#"+parts[1], text);
                
            } else {
                super.link(attributes, hrefOrHashName+".html", text);
            }
        }        
    }
    
    
}
