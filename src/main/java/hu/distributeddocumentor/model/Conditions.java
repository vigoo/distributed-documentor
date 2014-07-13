package hu.distributeddocumentor.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A set of enabled conditions for generating the documentation
 * @author Daniel Vigovszky
 */
public class Conditions {
    private final Set<String> conditions = new HashSet<>();

    public boolean allEnabled(Collection<String> required) {
        return conditions.containsAll(required);
    }

    public void enable(String condition) {
        conditions.add(condition);
    }

    public int getEnabledConditionsCount() {
        return conditions.size();
    }

    public Iterable<String> getEnabledConditions() {
        return conditions;
    }

    public void reset() {
        conditions.clear();
    }
    
    
}
