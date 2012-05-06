package hu.distributeddocumentor.model;

import hu.distributeddocumentor.model.builders.LinkFixingBuilder;
import hu.distributeddocumentor.model.builders.ExtendedHtmlDocumentBuilder;
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
    
    private final static String TEMPLATE = "= Title =\n\nBody\n";
    private static final Pattern SNIPPET_PATTERN = Pattern.compile("\\[Snippet\\:(\\w+)\\]");
    
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
    private final PageMetadata metadata;
    
    private boolean hasChanged;
    
    public Page(String id, SnippetCollection snippets) {
        this.id = id;
        this.snippets = snippets;
        
        metadata = new PageMetadata(id);
        
        markupLanguage = "MediaWiki";
        markup = TEMPLATE;
        isParserInitialized = false;        
        hasChanged = true;
        
        initializeParser();
        refs = refExtractor.getReferencedPages(markup);
    }
    
    public Page(File source, SnippetCollection snippets) throws FileNotFoundException, IOException {
        
        this.snippets = snippets;        
        
        final String fileName = source.getName();
        final int lastDot = fileName.lastIndexOf('.');
        id = fileName.substring(0, lastDot);
        markupLanguage = fixMarkupLanguage(fileName.substring(lastDot + 1));
        
        metadata = new PageMetadata(id);
        metadata.load(source.getParentFile());
        
        final FileInputStream stream = new FileInputStream(source);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
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
    
    private File getFile(File targetDirectory) {
        return new File(targetDirectory, id + "." + markupLanguage);
    }
    
    public File[] getFiles(File targetDirectory) {
        
        File[] files = new File[2];
        files[0] = getFile(targetDirectory);
        files[1] = metadata.getFile(targetDirectory);
        return files;
        
    }
    
    public File[] save(File targetDirectory) throws IOException {
        
        File[] targets = getFiles(targetDirectory);
        
        PrintWriter fwriter = new PrintWriter(new FileWriter(targets[0]));
        try {
            fwriter.print(markup);
            
            hasChanged = false;
        }
        finally {
            fwriter.close();
        }      
        
        metadata.save(targetDirectory);
        
        return targets;
    }

    public String getId() {
        return id;
    }    
    
    public PageMetadata getMetadata() {
        return metadata;
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
        
        return asHTML(false, null, false);
    }
    
    public String asHTMLembeddingCSS() {
        
        return asHTML(true, null, false);
    }
    
    public String asAnnotatedHTMLembeddingCSS() {
        return asHTML(true, null, true);
    }
    
    public String asHTMLembeddingCSS(File root) {
        
        return asHTML(true, root, false);
    }
    
    private String asHTML(boolean embedCSS, File root, boolean annotated) {
       if (!isParserInitialized)
           initializeParser();
       
       StringWriter writer = new StringWriter();
       
       ExtendedHtmlDocumentBuilder builder = new ExtendedHtmlDocumentBuilder(writer, root);
       
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

       parser.parse(preprocessMarkup(annotateMarkup(annotated, markup)));
       
       String fixed = StringUtils.replace(writer.toString(), "&#xc", " ");
       return fixed;
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
    
    public boolean saveIfModified(File root) throws IOException {
        
        if (hasChanged || metadata.hasChanged()) {
            save(root);  
            
            return true;
        } else {
            return false;
        }
    }    

    private void initializeParser() {
        ServiceLocator serviceLocator = ServiceLocator.getInstance();              
        language = serviceLocator.getMarkupLanguage(markupLanguage);                
        
        refExtractor = new PageRefExtractor(markupLanguage);
        
        isParserInitialized = true;
    }

    boolean equalsTemplate() {
        return markup.equals(TEMPLATE);
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

                Matcher matcher = SNIPPET_PATTERN.matcher(line);

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

    private String annotateMarkup(boolean annotated, String markup) {
        
        if (annotated) {
            
            List<String> lines = Arrays.asList(markup.split("\n"));
            StringBuilder result = new StringBuilder();
            
            for (int i = 0; i < lines.size(); i++) {
                
                String line = lines.get(i);
                
                int ipoint;
                int linkContext = 0;
                int tagContext = 0;
                boolean found = false;
                for (ipoint = 0; ipoint < line.length(); ipoint++) {                    
                    char ch = line.charAt(ipoint);
                    
                    if (ch == '[')
                        linkContext++;
                    else if (ch == '<')
                        tagContext++;
                    else if (ch == ']')
                        linkContext--;
                    else if (ch == '>')
                        tagContext--;
                    else if (linkContext == 0 && tagContext == 0 && Character.isLetterOrDigit(ch)) {
                        found = true;
                        break;
                    }
                }
                
                if (found) {
                    result.append(line.substring(0, ipoint))
                          .append("<span id=\"line")
                          .append(Integer.toString(i))
                          .append("\"/>")
                          .append(line.substring(ipoint))
                          .append('\n');                
                }
                else {
                    result.append(line).append('\n');
                }
            }
            
            return result.toString();
        } else {
            return markup;
        }
    }

}
