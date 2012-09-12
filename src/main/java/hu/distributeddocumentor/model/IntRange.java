package hu.distributeddocumentor.model;


/**
 * Class representing a closed integer range
 * 
 * @author Daniel Vigovszky
 */
public class IntRange {

    private final int start;
    private final int end;

    /**
     * Gets the last value of the range
     * 
     * @return the end of the range
     */
    public int getEnd() {
        return end;
    }

    /**
     * Gets the first value of the range
     * 
     * @return the start of the range
     */
    public int getStart() {
        return start;
    }
        

    /**
     * Creates a new range
     * 
     * @param start the first value of the range
     * @param end the last value of the range
     */
    public IntRange(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
    /**
     * Checks if a value belongs to the range
     * 
     * @param value value to be checked
     * @return true if the value is between the start and end values of the range
     */
    public boolean contains(int value) {
        return value >= start && value <= end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntRange other = (IntRange) obj;
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }
    
    
    @Override
    public String toString() {
        return "[" + start + ".." + end + ']';
    }
    
    
}
