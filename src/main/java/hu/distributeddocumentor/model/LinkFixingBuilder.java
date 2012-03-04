package hu.distributeddocumentor.model;

import java.io.Writer;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.util.XmlStreamWriter;

public class LinkFixingBuilder extends HtmlDocumentBuilder {

    public LinkFixingBuilder(XmlStreamWriter writer) {
        super(writer);
    }

    public LinkFixingBuilder(Writer out, boolean formatting) {
        super(out, formatting);
    }

    public LinkFixingBuilder(Writer out) {
        super(out);
    }
        
    @Override
    public void image(Attributes attributes, String url) {
        
        if (!url.startsWith("http://") && 
            !url.startsWith("https://") &&
            !url.startsWith("file://") &&
            !url.startsWith("media/"))
            url = "media/"+url;
        
        super.image(attributes, url);
    }

    @Override
    public void link(Attributes attributes, String hrefOrHashName, String text) {
        
        if (hrefOrHashName.startsWith("http://") ||
            hrefOrHashName.startsWith("https://") ||
            hrefOrHashName.startsWith("file://") ||
            hrefOrHashName.startsWith(".html")) {             
                super.link(attributes, hrefOrHashName, text);
        } else {
            super.link(attributes, hrefOrHashName+".html", text);
        }        
    }
    
    
}
