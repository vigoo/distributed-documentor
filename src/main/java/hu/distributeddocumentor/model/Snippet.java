package hu.distributeddocumentor.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Snippets are special page fragments which can be included into other pages
 * or snippets.
 * 
 * @author Daniel Vigovszky
 * @see Page
 */
public class Snippet extends Page {

    /**
     * Loads a snippet from the file system
     * 
     * @param source snippet file to load
     * @param snippets the snippet collection to be used when resolving snippet references in the markup
     * @param conditions enabled conditions
     * @param customStylesheet The documentation-specific custom stylesheet
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Snippet(File source, SnippetCollection snippets, Conditions conditions, File customStylesheet) throws FileNotFoundException, IOException {
        super(source, snippets, conditions, customStylesheet);
    }

    /**
     * Creates a new snippet
     * 
     * @param id the snippet's unique identifier
     * @param snippets the snippet collection to be used when resolving snippet references in the markup
     * @param conditions enabled conditions
     * @param customStylesheet The documentation-specific custom stylesheet
     */
    public Snippet(String id, SnippetCollection snippets, Conditions conditions, File customStylesheet) {
        super(id, snippets, conditions, customStylesheet);
    }   
}
