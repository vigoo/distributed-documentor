
package hu.distributeddocumentor.model.virtual.builders.merge;

import hu.distributeddocumentor.model.virtual.builders.VirtualNodeException;

/** Thrown when the merged documentation is missing
 *
 * @author Daniel Vigovszky
 */
public class MergedDocumentationIsMissingException extends VirtualNodeException {
    
    public MergedDocumentationIsMissingException(String location) {
        super(formatMessage(location));
    }
    
    public MergedDocumentationIsMissingException(String location, Throwable inner) {
        super(formatMessage(location), inner);
    }
    
    private static String formatMessage(String location) {
        return String.format("The documentation to be merged (%s) is missing!", location);
    }
}
