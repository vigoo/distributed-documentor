package hu.distributeddocumentor.exporters.html;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import hu.distributeddocumentor.exporters.Exporter;

/**
 * Guice module for the HTML exporter implementation
 * @author Daniel Vigovszky
 */
public class HTMLExporterModule extends AbstractModule {

    @Override
    protected void configure() {        
        Multibinder<Exporter> binder = Multibinder.newSetBinder(binder(), Exporter.class);
        binder.addBinding().to(HTMLExporter.class);
    }

}
