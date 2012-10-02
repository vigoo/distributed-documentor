package hu.distributeddocumentor.exporters.html;

import com.google.inject.AbstractModule;
import hu.distributeddocumentor.exporters.Exporter;

/**
 * Guice module for the HTML exporter implementation
 * @author Daniel Vigovszky
 */
public class HTMLExporterModule extends AbstractModule {

    @Override
    protected void configure() {        
        bind(Exporter.class).to(HTMLExporter.class);
    }

}
