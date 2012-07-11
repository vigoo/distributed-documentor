package hu.distributeddocumentor.model.virtual.builders;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.*;
import hu.distributeddocumentor.model.virtual.MediaWikiWriter;
import hu.distributeddocumentor.model.virtual.VirtualHierarchyBuilder;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class VirtualHierarchyBuilderBase implements VirtualHierarchyBuilder {
    private static final Logger log = LoggerFactory.getLogger(VirtualHierarchyBuilderBase.class);
    
    private final String markupLanguage;
    private final SnippetCollection emptySnippets;
    
    protected VirtualHierarchyBuilderBase(String markupLanguage) {
        this.markupLanguage = markupLanguage;
        
        emptySnippets = new SnippetCollection() {

            @Override
            public Collection<Snippet> getSnippets() {
                return new LinkedList<>();
            }

            @Override
            public Snippet getSnippet(String id) {
                return null;
            }

            @Override
            public void addSnippet(Snippet snippet) throws IOException, PageAlreadyExistsException {                
            }

            @Override
            public void removeSnippet(String id) {
            }
        };
    }

    protected TOCNode createNode(String title, Page page) {
        
        return new TOCNode(title, page);        
    }
    
    protected Page virtualPage(String id, Function<WikiWriter, Void> renderer) {
        
        StringWriter stringWriter = new StringWriter();
        WikiWriter wikiWriter = createWriter(stringWriter);
        
        renderer.apply(wikiWriter);
        stringWriter.flush();
        
        Page result = new Page(id, emptySnippets);
        result.setMarkupLanguage(markupLanguage);
        result.setMarkup(stringWriter.toString());
        
        //log.debug(result.getMarkup());
        
        return result;
    }
    
    protected String generateId() {
        return UUID.randomUUID().toString();
    }
    
    protected String generateId(String baseName, String itemFullName) {
        
        String result = baseName + "__" + itemFullName;
        return result
                .replace('.', '_')
                .replace("`", "___");
    }
    
    private WikiWriter createWriter(Writer out) {
    
        switch (markupLanguage) {
            case "MediaWiki":
                return new MediaWikiWriter(out);
            default:
                throw new UnsupportedOperationException("Markup language not supported");
                
        }

    }
}
