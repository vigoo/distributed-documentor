package hu.distributeddocumentor.exporters.chm;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import hu.distributeddocumentor.exporters.Exporter;

/**
 * Guice module for the CHM exporter implementation
 * @author Daniel Vigovszky
 */
public class CHMExporterModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<Exporter> binder = Multibinder.newSetBinder(binder(), Exporter.class);
        binder.addBinding().to(CHMExporter.class);
    }

}
