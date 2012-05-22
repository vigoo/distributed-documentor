package hu.distributeddocumentor.prefs;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentorPreferences {

    private static final Logger logger = LoggerFactory.getLogger(DocumentorPreferences.class.getName());
    
    private final Preferences prefs;
    private final Options cmdOptions;
    private final Option docRootOption;
    private final Option exportTargetOption;
    private final Option exportHTMLOption;
    private final Option exportCHMOption;
    private final CommandLine cmdLine;
    
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
    }
       
    public String getMercurialPath() {        
        return prefs.get("hgpath", null);
    }
    
    public void setMercurialPath(String path) {
        prefs.put("hgpath", path);
    }
    
    public String getCHMCompilerPath() {
        return prefs.get("hhcpath", null);
    }
    
    public void setCHMCompilerPath(String path) {
        prefs.put("hhcpath", path);
    }
    
    public List<String> getRecentRepositories() {        
        
        try {
            List<String> result = new LinkedList<String>();
            Preferences reposNode = prefs.node("recentRepositories");

            for (String key : reposNode.keys()) {
                String item = reposNode.get(key, null);
                if (item != null)
                    result.add(item);
            }

            return result;
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
            return new LinkedList<String>();
        }
    }
    
    public void setRecentRepositories(List<String> list) {
        
        try {
            Preferences reposNode = prefs.node("recentRepositories");

            for (String key : reposNode.keys())
                reposNode.remove(key);
            
            for (int i = 0; i < list.size(); i++) {
                reposNode.put(Integer.toString(i), list.get(i));
            }
            
            prefs.flush();
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
        }
    }
    
     public List<String> getRecentTargets() {        
        
        try {
            List<String> result = new LinkedList<String>();
            Preferences reposNode = prefs.node("recentTargets");

            for (String key : reposNode.keys()) {
                String item = reposNode.get(key, null);
                if (item != null)
                    result.add(item);
            }

            return result;
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
            return new LinkedList<String>();
        }
    }
    
    public void setRecentTargets(List<String> list) {
        
        try {
            Preferences reposNode = prefs.node("recentTargets");

            for (String key : reposNode.keys())
                reposNode.remove(key);
            
            for (int i = 0; i < list.size(); i++) {
                reposNode.put(Integer.toString(i), list.get(i));
            }
            
            prefs.flush();
        }
        catch (BackingStoreException ex) {
            logger.error(null, ex);
        }
    }
 
    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public boolean hasValidMercurialPath() {
        String path = getMercurialPath();
        
        if (path != null) {
            
            File f = new File(path);
            if (f.exists() && f.canExecute())
                return true;
        }
        
        return false;
    }

    public boolean hasValidCHMCompilerPath() {
        String path = getCHMCompilerPath();
        
        if (path != null) {
            
            File f = new File(path);
            if (f.exists() && f.canExecute())
                return true;
        }
        
        return false;
    }
    
    public boolean exportToHTML() {
        if (cmdLine != null)
            return cmdLine.hasOption("html") && 
                   cmdLine.hasOption("target") &&
                   cmdLine.hasOption("root");
        else
            return false;
    }
    
    public boolean exportToCHM() {
        if (cmdLine != null)
            return cmdLine.hasOption("chm") && 
                   cmdLine.hasOption("target") &&
                   cmdLine.hasOption("root");
        else
            return false;
    }
    
    public File getInitialRoot() {
        if (cmdLine != null) {
            if (cmdLine.hasOption("root"))
                return new File(cmdLine.getOptionValue("root"));
        }
        
        return null;
    }
    
    public File getExportTarget() {
        if (cmdLine != null) {
            if (cmdLine.hasOption("target"))
                return new File(cmdLine.getOptionValue("target"));           
        }
        
        return null;
    }
}
