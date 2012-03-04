/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.distributeddocumentor.model;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;

/**
 *
 * @author vigoo
 */
public class Page extends Observable {
    
    private final static String template = "= Title =\r\n\r\nBody\r\n";
    
    private final String id;
    private String markupLanguage;
    private String markup;
    
    private List<String> refs;
    
    private boolean isParserInitialized;
    private MarkupParser parser;
    private PageRefExtractor refExtractor;    
    private MarkupLanguage language;
    
    private boolean hasChanged;
    
    public Page(String id) {
        this.id = id;
        markupLanguage = "MediaWiki";
        markup = template;
        isParserInitialized = false;        
        hasChanged = true;
        
        initializeParser();
        refs = refExtractor.getReferencedPages(markup);
    }
    
    public Page(File source) throws FileNotFoundException, IOException {
        
        String fileName = source.getName();
        int lastDot = fileName.lastIndexOf(".");
        id = fileName.substring(0, lastDot);
        markupLanguage = fixMarkupLanguage(fileName.substring(lastDot + 1));
        
        FileInputStream stream = new FileInputStream(source);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {        
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\r\n");
            }

            markup = builder.toString();
            
            initializeParser();
            refs = refExtractor.getReferencedPages(markup);
        }
        finally {
            reader.close();
            
            hasChanged = false;
        }
    }
    
    public File getFile(File targetDirectory) {
        return new File(targetDirectory, id + "." + markupLanguage);
    }
    
    public File save(File targetDirectory) throws IOException {
        
        File target = getFile(targetDirectory);
        
        PrintWriter fwriter = new PrintWriter(new FileWriter(target));
        try {
            fwriter.print(markup);
            
            hasChanged = false;
        }
        finally {
            fwriter.close();
        }      
        
        return target;
    }

    public String getId() {
        return id;
    }    

    public String getMarkup() {
        return markup;
    }

    public void setMarkup(String markup) {
        this.markup = markup;
              
        if (!isParserInitialized)
            initializeParser();
        
        refs = refExtractor.getReferencedPages(markup);
        
        setChanged();
        notifyObservers();       
        
        hasChanged = true;
    }

    public String getMarkupLanguage() {
        return markupLanguage;
    }

    public void setMarkupLanguage(String markupLanguage) {
        this.markupLanguage = markupLanguage;
        
        isParserInitialized = false;
        
        setChanged();
        notifyObservers();
        
        hasChanged = true;        
    }
    
    public String asHTML() {
       if (!isParserInitialized)
           initializeParser();
       
       StringWriter writer = new StringWriter();
       parser = new MarkupParser(language, new LinkFixingBuilder(writer));             

       parser.parse(markup);
       return writer.toString();
    }
    
    public List<String> getReferencedPages() {
        return Collections.unmodifiableList(refs);
    }
    
    public void saveIfModified(File root) throws IOException {
        
        if (hasChanged)
            save(root);
    }    

    private void initializeParser() {
        ServiceLocator serviceLocator = ServiceLocator.getInstance();              
        language = serviceLocator.getMarkupLanguage(markupLanguage);                
        
        refExtractor = new PageRefExtractor(markupLanguage);
        
        isParserInitialized = true;
    }

    boolean equalsTemplate() {
        return markup.equals(template);
    }

    private String fixMarkupLanguage(String substring) {
        
        if (substring.equalsIgnoreCase("mediawiki"))
            return "MediaWiki";
        else
            return substring;
        
    }

}
