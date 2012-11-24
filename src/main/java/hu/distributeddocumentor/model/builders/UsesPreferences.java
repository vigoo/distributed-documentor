package hu.distributeddocumentor.model.builders;

import hu.distributeddocumentor.prefs.DocumentorPreferences;

/**
 * Interface for builders which needs an DocumentorPrefereneces instance
 * @author Daniel Vigovszky
 */
public interface UsesPreferences {
    
    void setPreferences(DocumentorPreferences prefs);
}
