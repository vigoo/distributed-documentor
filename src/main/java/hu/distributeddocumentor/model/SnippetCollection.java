package hu.distributeddocumentor.model;

import java.io.IOException;
import java.util.Collection;

/**
 * Interface for a collection of snippets belonging to a documentation
 * 
 * @author Daniel Vigovszky
 * @see Snippet
 */
public interface SnippetCollection {

    /**
     * Gets every snippet
     * 
     * @return the collection of all the registered snippets
     */
    Collection<Snippet> getSnippets();
    
    /**
     * Gets a snippet by its unique identifier
     * 
     * @param id the snippet's identifier
     * @return the snippet or null if it does not exist
     */
    Snippet getSnippet(String id);
    
    /**
     * Adds a new snippet to the collection
     * 
     * @param snippet the snippet to be added
     * @throws IOException
     * @throws PageAlreadyExistsException
     */
    void addSnippet(Snippet snippet) throws IOException, PageAlreadyExistsException;
    
    /**
     * Removes a snippet by its identifier
     * @param id the snippet's identifier
     */
    void removeSnippet(String id);
}
