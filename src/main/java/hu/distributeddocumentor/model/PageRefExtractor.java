package hu.distributeddocumentor.model;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;

public class PageRefExtractor extends DocumentBuilder {
    
    private final MarkupParser parser;
    private List<String> refs;
    
    public PageRefExtractor(String markupLanguage) {
        ServiceLocator serviceLocator = ServiceLocator.getInstance();              
        MarkupLanguage language = serviceLocator.getMarkupLanguage(markupLanguage);                
        
        parser = new MarkupParser(language, this);     
    }
    
    public List<String> getReferencedPages(String markup) {
                
        refs = new LinkedList<String>();
        
        try {
            parser.parse(markup);        
            return refs;
        }
        finally{
            refs = null;
        }
    }

    @Override
    public void beginDocument() {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void beginBlock(BlockType type, Attributes attributes) {
    }

    @Override
    public void endBlock() {
    }

    @Override
    public void beginSpan(SpanType type, Attributes attributes) {        
    }

    @Override
    public void endSpan() {        
    }

    @Override
    public void beginHeading(int level, Attributes attributes) {        
    }

    @Override
    public void endHeading() {        
    }

    @Override
    public void characters(String text) {        
    }

    @Override
    public void entityReference(String entity) {        
    }

    @Override
    public void image(Attributes attributes, String url) {        
    }

    @Override
    public void link(Attributes attributes, String hrefOrHashName, String text) {        
        
        if (!hrefOrHashName.startsWith("http://")) {         
            if (!refs.contains(hrefOrHashName))
                if (!hrefOrHashName.startsWith("Snippet:"))
                    refs.add(hrefOrHashName);            
        }        
    }

    @Override
    public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl) {        
    }

    @Override
    public void acronym(String text, String definition) {        
    }

    @Override
    public void lineBreak() {        
    }

    @Override
    public void charactersUnescaped(String literal) {        
    }
    
}
