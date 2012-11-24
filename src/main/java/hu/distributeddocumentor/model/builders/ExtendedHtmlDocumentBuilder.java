package hu.distributeddocumentor.model.builders;

import hu.distributeddocumentor.utils.ResourceUtils;
import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;


/**
 * Extends the HTML document builder with syntax highlighting support.
 * 
 * <p>
 * The syntax highlight is implemented using the SyntaxHighlighter javascript
 * library: http://alexgorbatchev.com/SyntaxHighlighter/
 * 
 * @author Daniel Vigovszky
 */
public class ExtendedHtmlDocumentBuilder extends LinkFixingBuilder {

    private final Set<String> usedSyntaxes = new HashSet<>();
    private final String pathToRoot;
    
    private static Map<String, String> syntaxMap;
    
    {
        syntaxMap = new HashMap<>();
        syntaxMap.put("applescript", "AppleScript");
        syntaxMap.put("actionscript3", "AS3");
        syntaxMap.put("as3", "AS3");
        syntaxMap.put("bash", "Bash");
        syntaxMap.put("shell", "Bash");
        syntaxMap.put("coldfusion", "ColdFusion");
        syntaxMap.put("cf", "ColdFusion");
        syntaxMap.put("cpp", "Cpp");
        syntaxMap.put("c", "Cpp");
        syntaxMap.put("c#", "CSharp");
        syntaxMap.put("c-sharp", "CSharp");
        syntaxMap.put("csharp", "CSharp");
        syntaxMap.put("css", "Css");
        syntaxMap.put("delphi", "Delphi");
        syntaxMap.put("pascal", "Delphi");
        syntaxMap.put("pas", "Delphi");
        syntaxMap.put("diff", "Diff");
        syntaxMap.put("patch", "Diff");
        syntaxMap.put("erl", "Erlang");
        syntaxMap.put("erlang", "Erlang");
        syntaxMap.put("groovy", "Groovy");
        syntaxMap.put("java", "Java");
        syntaxMap.put("jfx", "JavaFX");
        syntaxMap.put("javafx", "JavaFX");
        syntaxMap.put("js", "JScript");
        syntaxMap.put("jscript", "JScript");
        syntaxMap.put("javascript", "JScript");
        syntaxMap.put("perl", "Perl");
        syntaxMap.put("Perl", "Perl");
        syntaxMap.put("pl", "Perl");
        syntaxMap.put("php", "Php");
        syntaxMap.put("text", "Plain");
        syntaxMap.put("plain", "Plain");
        syntaxMap.put("powershell", "PowerShell");
        syntaxMap.put("ps", "PowerShell");
        syntaxMap.put("py", "Python");
        syntaxMap.put("python", "Python");
        syntaxMap.put("ruby", "Ruby");
        syntaxMap.put("rb", "Ruby");
        syntaxMap.put("rails", "Ruby");
        syntaxMap.put("ror", "Ruby");
        syntaxMap.put("sass", "Sass");
        syntaxMap.put("scss", "Sass");
        syntaxMap.put("scala", "Scala");
        syntaxMap.put("sql", "Sql");
        syntaxMap.put("vb", "Vb");
        syntaxMap.put("vbnet", "Vb");
        syntaxMap.put("xml", "Xml");
        syntaxMap.put("xhtml", "Xml");
        syntaxMap.put("xslt", "Xml");
        syntaxMap.put("html", "Xml");
        syntaxMap.put("bat", "Bat");
        syntaxMap.put("cmd", "Bat");
        syntaxMap.put("batch", "Bat");
        syntaxMap.put("clojure", "Clojure");
        syntaxMap.put("Clojure", "Clojure");
        syntaxMap.put("clj", "Clojure");       
        syntaxMap.put("f#", "FSharp");
        syntaxMap.put("f-sharp", "FSharp");
        syntaxMap.put("fsharp", "FSharp");
    }
    
    /**
     * Constructs the HTML builder
     * 
     * @param out the writer to be used to generate the HTML output
     * @param root root directory for the documentation's html output
     * @param pathToRoot relative path to the root where scripts and stylesheets lie
     */
    public ExtendedHtmlDocumentBuilder(Writer out, File root, String pathToRoot) {
        super(out, root);
        
        this.pathToRoot = pathToRoot;
    }


    @Override
    protected void endBody() {
                        
        if (usedSyntaxes.size() > 0) {
                        
            emitScript("syntaxhighlighter/shCore.js");
            
            for (String syntax : usedSyntaxes) {
                emitScript("syntaxhighlighter/shBrush" + syntax + ".js");
            }
            
            emitStylesheet("syntaxhighlighter/shCore.css");
            emitStylesheet("syntaxhighlighter/shThemeDefault.css");                                
        
            writer.writeStartElement("script");
            writer.writeAttribute("type", "text/javascript");
            writer.writeCharacters("\nSyntaxHighlighter.all()\n");
            writer.writeEndElement();
        }
        
        super.endBody();
    }

    @Override
    public void beginBlock(BlockType type, Attributes attributes) {
        super.beginBlock(type, attributes);
        
        if (type == BlockType.PREFORMATTED) {
            
            String cls = attributes.getCssClass();
            if (cls != null && cls.startsWith("brush: ")) {
                String syntaxRef = cls.substring("brush: ".length());
                String syntax = syntaxMap.get(syntaxRef);
                
                if (syntax != null) {
                    usedSyntaxes.add(syntax);
                }
            }
        }
    }        

    private void emitStylesheet(String css) {
        
        writer.writeEmptyElement("link");
        writer.writeAttribute("type", "text/css");
        writer.writeAttribute("rel", "stylesheet");
        writer.writeAttribute("href", pathToRoot+css);
    }
    
    private void emitScript(String js) {
        writer.writeStartElement("script");
        writer.writeAttribute("type", "text/javascript");
        writer.writeAttribute("src", pathToRoot+js);        
        writer.writeEndElement();
    }
    
}
