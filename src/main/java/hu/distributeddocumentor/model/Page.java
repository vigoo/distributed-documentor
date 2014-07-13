package hu.distributeddocumentor.model;

import hu.distributeddocumentor.model.builders.ExtendedHtmlDocumentBuilder;
import hu.distributeddocumentor.model.builders.PageRefExtractor;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;

/**
 * One page of the documentation, represented in a markup language which can 
 * be converted to HTML.
 * 
 * <p>
 * The pages can refer to each other, can include snippets and can be referenced
 * from the TOC. Every page has an unique string identifier which can be used
 * in the markup languages to link to other pages.
 * <p>
 * Every page has an assigned set of metadata as well.
 * 
 * @author Daniel Vigovszky
 * @see Snippet
 * @see TOC
 * @see PageMetadata
 */
public class Page extends Observable {
    
    private final static String TEMPLATE = "= Title =\n\nBody\n";
    private static final Pattern SNIPPET_PATTERN = Pattern.compile("\\[Snippet\\:(\\w+)\\]");
    private static final Pattern CONDITIONAL_START_PATTERN = Pattern.compile("\\[When\\:(\\w+)\\]");
    private static final Pattern CONDITIONAL_END_PATTERN = Pattern.compile("\\[End\\]");
    
    private String id;
    private String markupLanguage;
    private String markup;
    
    private List<String> refs;
    private final Set<String> snippetRefs = new HashSet<>();
    
    private boolean isParserInitialized;
    private MarkupParser parser;
    private PageRefExtractor refExtractor;    
    private MarkupLanguage language;
    
    private final SnippetCollection snippets;    
    private final Conditions conditions;
    
    private PageMetadata metadata;
    
    private boolean hasChanged;
    
    /**
     * Creates a new page object
     * 
     * @param id the page's unique identifier
     * @param snippets snippet collection to be used when resolving snippet references
     * @param conditions enabled conditions
     */
    public Page(String id, SnippetCollection snippets, Conditions conditions) {
        this.id = id;
        this.snippets = snippets;
        
        metadata = new PageMetadata(id);
        
        markupLanguage = "MediaWiki";
        markup = TEMPLATE;
        isParserInitialized = false;        
        hasChanged = true;
        
        initializeParser();
        refs = refExtractor.getReferencedPages(markup);
        this.conditions = conditions;
    }
    
    /**
     * Loads a page object from the file system
     * 
     * @param source the file storing the page's markup
     * @param snippets the snippet collection to be used to resolve snippet references
     * @param conditions enabled conditions
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Page(File source, SnippetCollection snippets, Conditions conditions) throws FileNotFoundException, IOException {
        
        this.snippets = snippets;          
        this.conditions = conditions;
                
        load(source);
    }
    
    private File getFile(File targetDirectory) {
        return new File(targetDirectory, id + "." + markupLanguage);
    }
    
    /**
     * Gets the files used to store this page and all its related data
     * 
     * <p>
     * This method can be used to determine what files must be added to the repository
     * after the page has been saved.
     * 
     * @param targetDirectory the directory to be used when creating the paths
     * @return returns a list of file names belonging to the given targetDirectory
     */
    public File[] getFiles(File targetDirectory) {
        
        File[] files = new File[2];
        files[0] = getFile(targetDirectory);
        files[1] = metadata.getFile(targetDirectory);
        return files;
        
    }
    
    /**
     * Saves the page and all its metadata to the given target directory
     * 
     * @param targetDirectory the target directory to be used
     * @return returns a list of file names belonging to the given targetDirectory 
     *         which were created by this method.
     * @throws IOException
     */
    public File[] save(File targetDirectory) throws IOException {
        
        File[] targets = getFiles(targetDirectory);
        
        try (PrintWriter fwriter = new PrintWriter(new FileWriter(targets[0]))) {
            
            fwriter.print(markup);            
            hasChanged = false;
        }      
        
        metadata.save(targetDirectory);
        
        return targets;
    }

    /**
     * Gets the unique identifier of the page
     * 
     * @return the page identifier
     */
    public String getId() {
        return id;
    }    
    
    /**
     * Gets the associated metadata for this page
     
     * @return the metadata of the page (such as its status, etc.)
     * @see PageMetadata
     */
    public PageMetadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the page markup 
     * 
     * @return the page's source in its selected markup language
     */
    public String getMarkup() {
        return markup;
    }

    /**
     * Sets the page markup and process it immediately
     * 
     * @param markup the page's source in its selected markup language
     */
    public void setMarkup(String markup) {
        if (!this.markup.equals(markup)) {
            this.markup = markup;        
              
            if (!isParserInitialized) {
                initializeParser();
            }

            refs = refExtractor.getReferencedPages(preprocessMarkup(markup));

            setChanged();
            notifyObservers();       

            hasChanged = true;
        }
    }

    /**
     * Gets the markup language used by this page
     * 
     * <p>
     * Currently only MediaWiki is supported!
     * 
     * @return the markup language
     */
    public String getMarkupLanguage() {
        return markupLanguage;
    }

    /**
     * Sets the markup language used by this page
     * 
     * <p>
     * Currently only MediaWiki is supported!
     * 
     * @param markupLanguage the markup language to be used by this page
     */
    public void setMarkupLanguage(String markupLanguage) {
        this.markupLanguage = markupLanguage;
        
        isParserInitialized = false;
        
        setChanged();
        notifyObservers();
        
        hasChanged = true;        
    }
    
    /**
     * Gets the page's contents in HTML
     * 
     * <p>
     * The HTML will contain external references to the documentation's CSS 
     * file. Use asHTMLembeddingCSS method to make the stylesheet inline.
     * 
     * @param pathToRoot relative path to the root where scripts and stylesheets lie
     * 
     * @return returns the page markup converted to HTML
     */
    public String asHTML(String pathToRoot) {
        
        return asHTML(false, null, false, pathToRoot);
    }
    
    /**
     * Gets the page's contents in HTML with the stylesheet embedded into it.
     * 
     * <p>
     * This is useful for preview rendering, where the HTML and its CSS file
     * is not required to be saved to the file system.
     * <p>
     * Use asAnnotatedHTMLembeddingCSS method to get anchor points which can
     * be used to synchronize the preview with the editor!
     * 
     * @return returns the page markup converted to HTML
     */
    public String asHTMLembeddingCSS() {
        
        return asHTML(true, null, false, "");
    }
    
    /**
     * Gets the page's contents in HTML with the stylesheet embedded into it,
     * having annotations in the code which connect the generated HTML with 
     * its original markup.
     * 
     * @return returns the page markup converted to HTML
     */
    public String asAnnotatedHTMLembeddingCSS() {
        return asHTML(true, null, true, "");
    }
    
    /**
     * Gets the page's contents in HTML with the stylesheet embedded into it.
     * 
     * <p>
     * This is useful for preview rendering, where the HTML and its CSS file
     * is not required to be saved to the file system.
     * <p>
     * Use asAnnotatedHTMLembeddingCSS method to get anchor points which can
     * be used to synchronize the preview with the editor!
     * 
     * @param root the root directory to be added to relative links in the generated
     *        HTML source
     * @return returns the page markup converted to HTML
     */
    public String asHTMLembeddingCSS(File root) {
        
        return asHTML(true, root, false, "");
    }
    
    private String asHTML(boolean embedCSS, File root, boolean annotated, String pathToRoot) {
       if (!isParserInitialized) {
            initializeParser();
        }
       
       StringWriter writer = new StringWriter();
       
       ExtendedHtmlDocumentBuilder builder = new ExtendedHtmlDocumentBuilder(writer, root, pathToRoot);
       
       HtmlDocumentBuilder.Stylesheet stylesheet;
       if (embedCSS) {
           stylesheet = new HtmlDocumentBuilder.Stylesheet(
                   new InputStreamReader(
                    getClass().getResourceAsStream("/documentation.css")));
       } else {
           stylesheet = new HtmlDocumentBuilder.Stylesheet(pathToRoot+"documentation.css");
       }
       builder.addCssStylesheet(stylesheet);
       
       parser = new MarkupParser(language, builder);             

       parser.parse(preprocessMarkup(annotateMarkup(annotated, markup)));
       
       String fixed = StringUtils.replace(writer.toString(), "&#xc", " ");
       return fixed;
    }
    
    /**
     * Gets the list of referenced pages
     * 
     * @return identifies of every page referenced in the markup
     */
    public List<String> getReferencedPages() {
        return Collections.unmodifiableList(refs);
    }    
    
    
    /**
     * Checks if the page has reference to a given snippet
     * 
     * @param snippet the examined snippet
     * @return true if the page's markup references this particular snippet
     * @see Snippet
     */
    public boolean referencesSnippet(Snippet snippet) {
        
        return snippetRefs.contains(snippet.getId());
    }

    /**
     * Indicates that the page has been changed and attached views must be
     * refreshed.
     */
    public void refresh() {
        hasChanged = true;
        
        setChanged();
        notifyObservers();
    }
    
    /**
     * Saves the page and its metadata to the file system but only if
     * they have changed.
     * 
     * @param root target directory to be used
     * @return returns true if the files have been modified
     * @throws IOException
     */
    public boolean saveIfModified(File root) throws IOException {
        
        if (hasChanged || metadata.hasChanged()) {
            save(root);  
            
            return true;
        } else {
            return false;
        }
    }    
    
    public void modifyPageReferences(String oldId, String newId) {
        // TODO: this should belong to a markup language specific location        
        setMarkup(
            markup.replaceAll("(?i)\\[\\["+oldId+"\\]\\]", "[["+newId+"]]")
                  .replaceAll("(?i)\\["+oldId+"\\]", "["+newId+"]")
                  .replaceAll("(?i)\\[\\["+oldId+"\\|([a-zA-Z0-9,!\\?\\. ]+)\\]\\]", "[["+newId+"|$1]]")
                  .replaceAll("(?i)\\["+oldId+" ([a-zA-Z0-9,!\\?\\. ]+)\\]", "["+newId+" $1]"));
    }

    private void initializeParser() {
        ServiceLocator serviceLocator = ServiceLocator.getInstance();              
        language = serviceLocator.getMarkupLanguage(markupLanguage);
        language.setInternalLinkPattern("{0}");
        
        refExtractor = new PageRefExtractor(markupLanguage);
        
        isParserInitialized = true;
    }

    public boolean equalsTemplate() {
        return markup.equals(TEMPLATE);
    }

    private String fixMarkupLanguage(String substring) {
        
        if (substring.equalsIgnoreCase("mediawiki")) {
            return "MediaWiki";
        }
        else {
            return substring;
        }
        
    }
    
    private String preprocessSnippets(String markup) {
        boolean mayHaveSnippetRefs = true;
        snippetRefs.clear();

        while (mayHaveSnippetRefs) {
            mayHaveSnippetRefs = false;
            List<String> lines = Arrays.asList(markup.split("\n"));
            List<String> resultLines = new LinkedList<>();

            for (String line : lines) {
                Matcher snippetMatcher = SNIPPET_PATTERN.matcher(line);
                if (snippetMatcher.matches()) {

                    String snippetId = snippetMatcher.group(1);
                    snippetRefs.add(snippetId);

                    Snippet snippet = snippets.getSnippet(snippetId);
                    if (snippet != null) {
                        resultLines.add(snippet.getMarkup());
                        mayHaveSnippetRefs = true;
                    }
                } else {
                    resultLines.add(line);
                }
            }

            markup = StringUtils.join(resultLines, '\n');
        }

        return markup;
    }

    private String preprocessConditionals(String markup) {
        Stack<String> conditionalStack = new Stack<>();
        List<String> lines = Arrays.asList(markup.split("\n"));
        List<String> resultLines = new LinkedList<>();

        for (String line : lines) {
            Matcher conditionalStartMatcher = CONDITIONAL_START_PATTERN.matcher(line);
            Matcher conditionalEndMatcher = CONDITIONAL_END_PATTERN.matcher(line);

            if (conditionalStartMatcher.matches()) {
                String condition = conditionalStartMatcher.group(1);
                conditionalStack.push(condition);
            } else if (conditionalEndMatcher.matches()) {
                if (!conditionalStack.empty()) {
                    conditionalStack.pop();
                }
            } else {
                if (conditions.allEnabled(conditionalStack)) {
                    resultLines.add(line);
                }
            }
        }
        markup = StringUtils.join(resultLines, '\n');

        return markup;

    }

    private String preprocessMarkup(String markup) {
        return preprocessConditionals(
                preprocessSnippets(markup));
    }

    private String annotateMarkup(boolean annotated, String markup) {
        
        if (annotated) {
            
            List<String> lines = Arrays.asList(markup.split("\n"));
            StringBuilder result = new StringBuilder();
            
            boolean preContext = false;
            for (int i = 0; i < lines.size(); i++) {
                
                String line = lines.get(i);
                               
                if (preContext && line.startsWith("</pre>")) {
                    // End of <pre> block
                    preContext = false;
                }
                else if (!preContext) {
                    
                    if (line.startsWith("<pre")) {
                        // Start of <pre> block
                        preContext = true;
                    } else {
                        // Not in <pre> block
                        
                        int ipoint;
                        int linkContext = 0;
                        int tagContext = 0;
                        boolean found = false;
                        for (ipoint = 0; ipoint < line.length(); ipoint++) {                    
                            char ch = line.charAt(ipoint);

                            if (ch == '[') {
                                linkContext++;
                            }
                            else if (ch == '<') {
                                tagContext++;
                            }
                            else if (ch == ']') {
                                linkContext--;
                            }
                            else if (ch == '>') {
                                tagContext--;
                            }
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

    /**
     * Reloads a page from the file system, optionally changing its id 
     * 
     * This is useful when a page has been renamed for example.
     * @param documentationDirectory documentation's root directory where the page files lie
     * @param newId new id, can be null if the id has not been changed
     */
    void reload(File documentationDirectory, String newId) throws IOException, FileNotFoundException {
        if (newId != null && !id.equals(newId)) {
            id = newId;
            metadata.changeId(newId);
        }
        
        load(new File(documentationDirectory, id+"."+markupLanguage));
        
        setChanged();            
        notifyObservers();       
    }

    private void load(File source) throws IOException, FileNotFoundException {
        final String fileName = source.getName();
        final int lastDot = fileName.lastIndexOf('.');
        
        id = fileName.substring(0, lastDot);
        markupLanguage = fixMarkupLanguage(fileName.substring(lastDot + 1));
        
        metadata = new PageMetadata(id);
        metadata.load(source.getParentFile());
        
        final FileInputStream stream = new FileInputStream(source);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {     
            
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
            hasChanged = false;
        }
    }

}
