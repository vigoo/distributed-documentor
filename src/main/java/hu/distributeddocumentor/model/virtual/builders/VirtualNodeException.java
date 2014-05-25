package hu.distributeddocumentor.model.virtual.builders;

/** Base class for exceptions thrown while processing virtual nodes
 *
 * @author Daniel Vigovszky
 */
public class VirtualNodeException extends Exception {

    public VirtualNodeException(String message) {
        super(message);
    }

    public VirtualNodeException(String message, Throwable cause) {
        super(message, cause);
    }    
}
