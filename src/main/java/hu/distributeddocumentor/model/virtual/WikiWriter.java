package hu.distributeddocumentor.model.virtual;

import java.io.IOException;
import java.net.URI;

/**
 * A markup-independent interface for emitting wiki code
 * 
 * @author Daniel Vigovszky
 */
public interface WikiWriter {

    /**
     * Write a heading
     * 
     * @param level heading level, 1 is the highest
     * @param title the heading title
     * @throws IOException 
     */
    void heading(int level, String title) throws IOException;
    
    /**
     * Start a new paragraph
     * @throws IOException 
     */
    void newParagraph() throws IOException;
    
    /**
     * Begin an indented block
     * @param level indentation level, must be 1 or greater
     */
    void beginIndent(int level);
    
    /**
     * Finish indenting
     */
    void endIndent();
    
    /**
     * Begins a bullet list item
     * @param level list depth, must be 1 or greater
     * @throws IOException 
     */
    void beginBullet(int level) throws IOException;
    
    /**
     * Begins an enumeration list item
     * @param level list depth, must be 1 or greater
     * @throws IOException 
     */
    void beginEnumerationItem(int level) throws IOException;
    
    /**
     * Writes a syntax highlighted source code block
     * @param lang source code language
     * @param code source code
     * @throws IOException 
     */
    void sourceCode(String lang, String code) throws IOException;
    
    /**
     * Writes a simple text.section
     * @param text the text to write
     * @throws IOException 
     */
    void text(String text) throws IOException;
    
    /**
     * Writes a bold text section
     * @param text the bold text to write
     * @throws IOException 
     */
    void bold(String text) throws IOException;
    
    /**
     * Writes an italic text section
     * @param text the italic text to write
     * @throws IOException 
     */
    void italic(String text) throws IOException;
    
    /**
     * Writes a bold and italic text section
     * @param text the bold and italic text to write
     * @throws IOException 
     */
    void boldItalic(String text) throws IOException;
    
    /**
     * Writes an image link
     * @param name name of the image
     * @throws IOException 
     */
    void image(String name) throws IOException;
    
    /**
     * Writes an internal link to another wiki page
     * @param id page identifier
     * @param text text of the link
     * @throws IOException 
     */
    void internalLink(String id, String text) throws IOException;
    
    /**
     * Writes an external link
     * @param uri URL to the linked page
     * @param text text of the link
     * @throws IOException 
     */
    void externalLink(URI uri, String text) throws IOException;
}
