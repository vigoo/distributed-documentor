package hu.distributeddocumentor.model;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;

public class Page extends Observable {
    
    private final static String template = "= Title =\n\nBody\n";
    
    private final String id;
    private String markupLanguage;
    private String markup;
    
    private List<String> refs;
    private Set<String> snippetRefs = new HashSet<String>();
    
    private boolean isParserInitialized;
    private MarkupParser parser;
    private PageRefExtractor refExtractor;    
    private MarkupLanguage language;
    
    private final SnippetCollection snippets;
    
    private static final Pattern snippetPattern = Pattern.compile("\\[Snippet\\:(\\w+)\\]");
    
    private boolean hasChanged;
    
    public Page(String id, SnippetCollection snippets) {
        this.id = id;
        this.snippets = snippets;
        
        markupLanguage = "MediaWiki";
        markup = template;
        isParserInitialized = false;        
        hasChanged = true;
        
        initializeParser();
        refs = refExtractor.getReferencedPages(markup);
    }
    
    public Page(File source, SnippetCollection snippets) throws FileNotFoundException, IOException {
        
        this.snippets = snippets;
        
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
                builder.append("\n");
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
        
        refs = refExtractor.getReferencedPages(preprocessMarkup(markup));
        
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
        
        return asHTML(false, null);
    }
    
    public String asHTMLembeddingCSS() {
        
        return asHTML(true, null);
    }
    
    public String asHTMLembeddingCSS(File root) {
        
        return asHTML(true, root);
    }
    
    private String asHTML(boolean embedCSS, File root) {
       if (!isParserInitialized)
           initializeParser();
       
       StringWriter writer = new StringWriter();
       
       HtmlDocumentBuilder builder = new LinkFixingBuilder(writer, root);
       
       HtmlDocumentBuilder.Stylesheet stylesheet;
       if (embedCSS) {
           stylesheet = new HtmlDocumentBuilder.Stylesheet(
                   new InputStreamReader(
                    getClass().getResourceAsStream("/documentation.css")));
       } else {
           stylesheet = new HtmlDocumentBuilder.Stylesheet("documentation.css");
       }
       builder.addCssStylesheet(stylesheet);
       
       parser = new MarkupParser(language, builder);             

       parser.parse(preprocessMarkup(markup));
       return writer.toString();
    }
    
    public List<String> getReferencedPages() {
        return Collections.unmodifiableList(refs);
    }    
    
    
    public boolean referencesSnippet(Snippet snippet) {
        
        return snippetRefs.contains(snippet.getId());
    }

    public void refresh() {
        hasChanged = true;
        
        setChanged();
        notifyObservers();
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
    
    private String preprocessMarkup(String markup) {
        
        boolean mayHaveSnippetRefs = true;
        snippetRefs.clear();
        
        while (mayHaveSnippetRefs) {
            
            mayHaveSnippetRefs = false;
                        
            List<String> lines = Arrays.asList(markup.split("\n"));
            List<String> resultLines = new LinkedList<String>();

            for (String line : lines) {

                Matcher matcher = snippetPattern.matcher(line);

                if (matcher.matches()) {

                    String snippetId = matcher.group(1);
                    snippetRefs.add(snippetId);

                    Snippet snippet = snippets.getSnippet(snippetId);
                    if (snippet != null) {                    
                        resultLines.add(snippet.getMarkup());
                        mayHaveSnippetRefs =true;
                    }
                } else {
                    resultLines.add(line);
                }
            }                 

            markup = StringUtils.join(resultLines, '\n');
        }
        
        return markup;
    }

}
