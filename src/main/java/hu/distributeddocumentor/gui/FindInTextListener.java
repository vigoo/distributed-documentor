
package hu.distributeddocumentor.gui;

/**
 * Interface for text editor components toward the find-in-text panel
 * @author Daniel Vigovszky
 */
public interface FindInTextListener {

    /**
     * Finds the next occurrence of the given text and select it
     * @param text Text to look for
     */
    void findNext(String text);
    
    /**
     * Finish the find in text mode
     */
    void finish();
}
