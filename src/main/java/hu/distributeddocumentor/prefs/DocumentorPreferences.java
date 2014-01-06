package hu.distributeddocumentor.prefs;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import hu.distributeddocumentor.exporters.Exporter;
import hu.distributeddocumentor.exporters.chm.CHMExporterModule;
import hu.distributeddocumentor.exporters.html.HTMLExporterModule;
import hu.distributeddocumentor.vcs.VersionControl;
import hu.distributeddocumentor.vcs.mercurial.MercurialVersionControl;
import java.awt.Font;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentorPreferences extends Observable {

    private static final Logger logger = LoggerFactory.getLogger(DocumentorPreferences.class.getName());
    
    private final Preferences prefs;
    private final Options cmdOptions;
    private final Option docRootOption;
    private final Option exportTargetOption;
    private final Option exportHTMLOption;
    private final Option exportCHMOption;
    private final CommandLine cmdLine;
    
    private final Injector injector;
    
    public DocumentorPreferences(String[] args) {
        
        prefs = Preferences.userRoot().node(this.getClass().getName());        
        
        docRootOption = OptionBuilder.withArgName("path")
                                     .hasArg()
                                     .withDescription("open documentation from the given root directory")
                                     .create("root");
        
        exportTargetOption = OptionBuilder.withArgName("path")
                                          .hasArg()
                                          .withDescription("target directory for the export operation")
                                          .create("target");
        
        exportHTMLOption = new Option("html", "export to static HTML pages");
        exportCHMOption = new Option("chm", "export to CHM");
        
        cmdOptions = new Options();
        cmdOptions.addOption(docRootOption);
        cmdOptions.addOption(exportTargetOption);
        cmdOptions.addOption(exportHTMLOption);
        cmdOptions.addOption(exportCHMOption);
        
        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(cmdOptions, args);            
        }
        catch (ParseException ex) {
            System.err.println("Command line parsing failed. Reason: " + ex.getMessage());                        
        }
        
        cmdLine = line;
        
        injector = Guice.createInjector(
                new HTMLExporterModule(),
                new CHMExporterModule(),
                new AbstractModule() {

                    @Override
                    protected void configure() {
                        bind(DocumentorPreferences.class).toInstance(DocumentorPreferences.this);
                        bind(VersionControl.class).to(MercurialVersionControl.class);
                    }                    
                });        
    }

    public Injector getInjector() {
        return injector;
    }    
    
    public String getMercurialPath() {        
        return prefs.get("hgpath", null);
    }
    
    public void setMercurialPath(String path) {
        prefs.put("hgpath", path);
        fireChanged("hgpath");
    }
    
    public String getCHMCompilerPath() {
        return prefs.get("hhcpath", null);        
    }
    
    public void setCHMCompilerPath(String path) {
        prefs.put("hhcpath", path);
        fireChanged("hhcpath");
    }
    
    public Exporter getDefaultExporter() {
        String exporterTargetName = getDefaultExporterName();
        ExporterLookup lookup = injector.getInstance(ExporterLookup.class);
        return lookup.getByTargetName(exporterTargetName);
    }
    
    public String getDefaultExporterName() {
        return prefs.get("defaultexporter", null);        
    }
    
    public void setDefaultExporter(Exporter exporter) {
        prefs.put("defaultexporter", exporter.getTargetName());
        fireChanged("defaultexporter");
    }
    
    public List<String> getRecentRepositories() {        
        return getRecentPaths("recentRepositories");
    }
    
    public void setRecentRepositories(List<String> list) {
        
        try {
            Preferences reposNode = prefs.node("recentRepositories");

            for (String key : reposNode.keys()) {
                reposNode.remove(key);
            }
            
            for (int i = 0; i < list.size(); i++) {
                reposNode.put(Integer.toString(i), list.get(i));
            }
            
            fireChanged("recentRepositories");
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
        }
    }
    
     public List<String> getRecentTargets() {        
               
         return getRecentPaths("recentTargets");        
    }
    
    public void setRecentTargets(List<String> list) {
        
        try {
            Preferences reposNode = prefs.node("recentTargets");

            for (String key : reposNode.keys()) {
                reposNode.remove(key);
            }
            
            for (int i = 0; i < list.size(); i++) {
                reposNode.put(Integer.toString(i), list.get(i));
            }
            
            fireChanged("recentTargets");
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
        }
    }
 
    public Font getEditorFont() {
        return Font.decode(prefs.get("editorfont", "Monaco 13"));
    }
    
    public void setEditorFont(Font font) {
        StringBuilder encoded = new StringBuilder(font.getFamily());
        if (font.isBold() && font.isItalic()) {
            encoded.append("-BOLDITALIC");
        }
        else if (font.isBold()) {
            encoded.append("-BOLD");
        }
        else if (font.isItalic()) {
            encoded.append("-ITALIC");
        }
        encoded.append("-");
        encoded.append(font.getSize());
        
        prefs.put("editorfont", encoded.toString());
        fireChanged("editorfont");
    }
    
    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public boolean hasValidMercurialPath() {
        String path = getMercurialPath();
        
        if (path != null) {
            
            File f = new File(path);
            if (f.exists() && f.canExecute()) {
                return true;
            }
        }
        
        return false;
    }

    public boolean hasValidCHMCompilerPath() {
        String path = getCHMCompilerPath();
        
        if (path != null) {
            
            File f = new File(path);
            if (f.exists() && f.canExecute()) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean exportToHTML() {
        if (cmdLine != null) {
            return cmdLine.hasOption("html") && 
                   cmdLine.hasOption("target") &&
                   cmdLine.hasOption("root");
        }
        else {
            return false;
        }
    }
    
    public boolean exportToCHM() {
        if (cmdLine != null) {
            return cmdLine.hasOption("chm") && 
                   cmdLine.hasOption("target") &&
                   cmdLine.hasOption("root");
        }
        else {
            return false;
        }
    }
    
    public File getInitialRoot() {
        if (cmdLine != null) {
            if (cmdLine.hasOption("root")) {
                return new File(cmdLine.getOptionValue("root"));
            }
        }
        
        return null;
    }
    
    public File getExportTarget() {
        if (cmdLine != null) {
            if (cmdLine.hasOption("target")) {
                return new File(cmdLine.getOptionValue("target"));
            }           
        }
        
        return null;
    }

    public boolean isSpellCheckingEnabled() {
        return prefs.getBoolean("spellchecking", true);
    }
    
    public void setSpellChecking(boolean enabled) {
        prefs.putBoolean("spellchecking", enabled);
        fireChanged("spellchecking");
    }
    
    public PreviewMode getPreviewMode() {
        return PreviewMode.valueOf(PreviewMode.class, prefs.get("previewmode", PreviewMode.VerticalSplit.name()));
    }
    
    public void setPreviewMode(PreviewMode mode) {
        prefs.put("previewmode", mode.name());
        fireChanged("previewmode");
    }
    
    public double getPreviewSplitterPos() {
        return prefs.getDouble("previewsplitterpos", 0.5);
    }
    
    public void setPreviewSplitterPos(double newPos) {
        prefs.putDouble("previewsplitterpos", newPos);
        fireChanged("previewsplitterpos");
    }
    
    public void toggleSpellChecking() {
        setSpellChecking(!isSpellCheckingEnabled());
    }
    
    private List<String> getRecentPaths(String optionName) {                
        try {            
            Preferences reposNode = prefs.node(optionName);
            Map<String, String> map = new HashMap<>();

            for (String key : reposNode.keys()) {
                String item = reposNode.get(key, null);
                if (item != null) {
                    map.put(key, item);
                }
            }
            
            List<String> result = new LinkedList<>();
            List<String> keys = new LinkedList<>(map.keySet());
            Collections.sort(keys);
            
            for (String key : keys) {
                result.add(map.get(key));
            }

            return result;
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
            return new LinkedList<>();
        }
    }    

    private void fireChanged(String name) {
        try {
            prefs.flush();
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
        }
        
        setChanged();
        notifyObservers(name);
    }

    public int getFloatingPreviewWidth() {
        return prefs.getInt("floatingpreviewwidth", 640);
    }
    
    public void setFloatingPreviewWidth(int width) {
        prefs.putInt("floatingpreviewwidth", width);
    }

    public int getFloatingPreviewHeight() {
        return prefs.getInt("floatingpreviewheight", 480);
    }
    
    public void setFloatingPreviewHeight(int height) {
        prefs.putInt("floatingpreviewheight", height);
    }
    
    public int getFloatingPreviewX() {
        return prefs.getInt("floatingpreviewx", 0);
    }

    public void setFloatingPreviewX(int x) {
        prefs.putInt("floatingpreviewx", x);
    }
    
    public int getFloatingPreviewY() {
        return prefs.getInt("floatingpreviewy", 0);
    }

    public void setFloatingPreviewY(int y) {
        prefs.putInt("floatingpreviewy", y);
    }
    
    public int getMainWindowWidth() {
        return prefs.getInt("mainwndwidth", 1024);
    }
    
    public void setMainWindowWidth(int width) {
        prefs.putInt("mainwndwidth", width);
    }

    public int getMainWindowHeight() {
        return prefs.getInt("mainwndheight", 768);
    }
    
    public void setMainWindowHeight(int height) {
        prefs.putInt("mainwndheight", height);
    }
    
    public int getMainWindowX() {
        return prefs.getInt("mainwndx", 0);
    }

    public void setMainWindowX(int x) {
        prefs.putInt("mainwndx", x);
    }
    
    public int getMainWindowY() {
        return prefs.getInt("mainwndy", 0);
    }

    public void setMainWindowY(int y) {
        prefs.putInt("mainwndy", y);
    }
}
