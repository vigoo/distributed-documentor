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
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Snippet(File source, SnippetCollection snippets) throws FileNotFoundException, IOException {
        super(source, snippets);
    }

    /**
     * Creates a new snippet
     * 
     * @param id the snippet's unique identifier
     * @param snippets the snippet collection to be used when resolving snippet references in the markup
     */
    public Snippet(String id, SnippetCollection snippets) {
        super(id, snippets);
    }   
}
