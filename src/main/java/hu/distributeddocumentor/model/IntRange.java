package hu.distributeddocumentor.model;


public class IntRange {

    private final int start;
    private final int end;

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }
        

    public IntRange(int start, int end) {
        this.start = start;
        this.end = end;
    }
    
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
