package hu.distributeddocumentor.prefs;

import com.google.inject.Inject;
import hu.distributeddocumentor.exporters.Exporter;
import java.util.Set;


public class ExporterLookup {

    private final Set<Exporter> exporters;
    
    @Inject
    public ExporterLookup(Set<Exporter> exporters) {
        this.exporters = exporters;
    }
    
    public Set<Exporter> getAll() {
        return exporters;
    }
    
    public Exporter getByTargetName(String targetName) {
        for (Exporter exporter : exporters) {
            if (targetName == null || exporter.getTargetName().equals(targetName)) {
                return exporter;
            }            
        }
        
        return null;
    }
}
