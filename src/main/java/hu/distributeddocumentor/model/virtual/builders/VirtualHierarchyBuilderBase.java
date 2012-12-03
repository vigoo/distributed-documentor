package hu.distributeddocumentor.model.virtual.builders;

import hu.distributeddocumentor.model.toc.TOCNode;
import com.google.common.base.Function;
import hu.distributeddocumentor.model.*;
import hu.distributeddocumentor.model.virtual.MediaWikiWriter;
import hu.distributeddocumentor.model.virtual.VirtualHierarchyBuilder;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for virtual hierarchy builders
 * 
 * @author Daniel Vigovszky
 */
public abstract class VirtualHierarchyBuilderBase implements VirtualHierarchyBuilder {
    private static final Logger log = LoggerFactory.getLogger(VirtualHierarchyBuilderBase.class);
    
    private final String markupLanguage;
    private final SnippetCollection emptySnippets;
    
    /**
     * Initializes the builder
     * 
     * @param markupLanguage the markup language to be used for all the generated pages
     */
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

    @Override
    public String getScope() {
        return null;
    }

    @Override
    public Collection<File> getExtraImages() {
        return new HashSet<>();
    }        

    /**
     * Creates a new node referring to a generated page
     * @param title title of the node
     * @param page the generated virtual page to refer to
     * @return returns the TOC node created
     */
    protected TOCNode createNode(String title, Page page) {
        
        return new TOCNode(title, page);        
    }
    
    /**
     * Creates a virtual page to be referred by a node in the generated
     * hierarchy.
     * 
     * @param id page identifier, can be used to navigate between pages
     * @param renderer page renderer function, will be called with a {@link WikiWriter}
     *                 instance to generate the page's contents
     * @return returns the generated page
     */
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
    
    /**
     * Generates an unique page identifier
     * @return a unique page identifier
     */
    protected String generateId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generates a wiki-compatible identifier from a namespace-like pair of
     * strings.
     * 
     * @param baseName base name (for example the namespace)
     * @param itemFullName item name (for example a class name)
     * @return returns a string which contains the base name and the item name
     *         but special characters not compatible with page id syntax are
     *         replaced.
     */
    protected String generateId(String baseName, String itemFullName) {
        
        String result = baseName + "__" + itemFullName;
        return result
                .replace('.', '_')
                .replace("`", "___");
    }
    
    /**
     * Creates a {@link WikiWriter} implementation based on what markup language
     * was given to the constructor.
     * 
     * @param out the text writer to be used by the wiki writer
     * @return returns a wiki writer for the appropriate markup language
     */
    private WikiWriter createWriter(Writer out) {
    
        switch (markupLanguage) {
            case "MediaWiki":
                return new MediaWikiWriter(out);
            default:
                throw new UnsupportedOperationException("Markup language not supported");
                
        }

    }
       
}
