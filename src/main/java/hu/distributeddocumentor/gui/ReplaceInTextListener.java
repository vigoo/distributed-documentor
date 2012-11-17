
package hu.distributeddocumentor.gui;

/**
 * Interface for text editor components toward the replace-in-text component,
 * providing operation to select and replace text
 * 
 * @author Daniel Vigovszky
 */
public interface ReplaceInTextListener extends FindInTextListener {

    /**
     * Checks whether the given text is already selected in the text editor,
     * either manually or by the findNext method
     * @param text Text to be selected
     * @return true if the given text is already selected, otherwise false
     */
    public boolean hasSelection(String text);

    /**
     * Replaces the current selection with the given text
     * @param text The new text
     */
    public void replaceCurrent(String text);

    /**
     * Replaces every occurrence of the string input with the string output
     * @param input The text to search for
     * @param output The replacement
     */
    public void replaceAll(String input, String output);
    
}
