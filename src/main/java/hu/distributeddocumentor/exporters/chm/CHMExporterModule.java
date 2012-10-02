package hu.distributeddocumentor.exporters.chm;

import com.google.inject.AbstractModule;
import hu.distributeddocumentor.exporters.Exporter;


public class CHMExporterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Exporter.class).to(CHMExporter.class);
    }

}
