package hu.distributeddocumentor.model.virtual;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;

/**
 * WikiWriter implementation for MediaWiki syntax
 * 
 * @author Daniel Vigovszky
 */
public class MediaWikiWriter implements WikiWriter {
    
    private final Writer writer;
    private int indentLevel = 0;
    private boolean atLineBeginning = true;

    public MediaWikiWriter(Writer writer) {
        this.writer = writer;
    }        

    @Override
    public void heading(int level, String title) throws IOException {
        
        String eqs = StringUtils.repeat('=', level);
        
        writer.write(eqs);
        writer.write(' ');
        writer.write(title);        
        writer.write(' ');
        writer.write(eqs);
        newLine();
    }

    @Override
    public void newParagraph() throws IOException {
        
        if (!atLineBeginning)
            newLine();
        newLine();
    }

    @Override
    public void beginIndent(int level) throws IllegalStateException {
        
        if (!atLineBeginning)
            throw new IllegalStateException("Must be called when at the begin of a line");
        
        indentLevel = level;
    }

    @Override
    public void endIndent() throws IllegalStateException {
        
        if (!atLineBeginning)
            throw new IllegalStateException("Must be called when at the begin of a line");
        
        indentLevel = 0;
    }

    @Override
    public void beginBullet(int level) throws IOException, IllegalStateException {
        
        if (level <= 0)
            throw new IllegalArgumentException("level must be greater than 0");
        if (indentLevel != 0)
            throw new IllegalStateException("Cannot begin a bullet in indented mode");
        
        writer.write(StringUtils.repeat('*', level));
        writer.write(' ');
        
        atLineBeginning = false;
    }

    @Override
    public void beginEnumerationItem(int level) throws IOException, IllegalStateException {
        
        if (level <= 0)
            throw new IllegalArgumentException("level must be greater than 0");
        if (indentLevel != 0)
            throw new IllegalStateException("Cannot begin an enumeration in indented mode");
        
        writer.write(StringUtils.repeat('#', level));
        writer.write(' ');
        
        atLineBeginning = false;
    }

    @Override
    public void sourceCode(String lang, String code) throws IOException, IllegalStateException {
        
        if (indentLevel != 0)
            throw new IllegalStateException("Cannot begin an enumeration in indented mode");
        
        writer.write("<pre class=\"brush: ");
        writer.write(lang);
        writer.write("\">\n");
        writer.write(code);
        writer.write("\n</pre>");
        newLine();
    }

    @Override
    public void text(String text) throws IOException {
        
        String[] parts = text.split("\n");
        for (int i = 0; i < parts.length; i++) {
            
            simpleText(parts[i]);
            if (i != (parts.length - 1))
                newLine();
        }
        
        if (text.endsWith("\n"))
            newLine();
    }
    
    private void simpleText(String text) throws IOException {
    
        indentIfNecessary();
        
        if (text.length() > 0) {
            writer.write(text);        
            atLineBeginning = false;
        }
    }

    @Override
    public void bold(String text) throws IOException {
        
        String[] parts = text.split("\n");
        for (int i = 0; i < parts.length; i++) {
            
            simpleBold(parts[i]);
            if (i != (parts.length - 1))
                newLine();
        }
        
        if (text.endsWith("\n"))
            newLine();
    }
    
    private void simpleBold(String text) throws IOException {
        
        indentIfNecessary();
        
        if (text.length() > 0) {
            writer.write(wrap(text, "'''"));        
            atLineBeginning = false;
        }
    }

    @Override
    public void italic(String text) throws IOException {
        
        String[] parts = text.split("\n");
        for (int i = 0; i < parts.length; i++) {
            
            simpleItalic(parts[i]);
            if (i != (parts.length - 1))
                newLine();
        }
        
        if (text.endsWith("\n"))
            newLine();
    }
    
    private void simpleItalic(String text) throws IOException {
        
        indentIfNecessary();
        
        if (text.length() > 0) {
            writer.write(wrap(text, "''"));        
            atLineBeginning = false;
        }
    }

    @Override
    public void boldItalic(String text) throws IOException {
         
        String[] parts = text.split("\n");
        for (int i = 0; i < parts.length; i++) {
            
            simpleBoldItalic(parts[i]);
            if (i != (parts.length - 1))
                newLine();
        }
        
        if (text.endsWith("\n"))
            newLine();
    }
    
    private void simpleBoldItalic(String text) throws IOException {
        
        indentIfNecessary();
        
        if (text.length() > 0) {
            writer.write(wrap(text, "''''"));        
            atLineBeginning = false;
        }
    }

    @Override
    public void image(String name) throws IOException {
        
        indentIfNecessary();
        writer.write("[[Image:");
        writer.write(name);
        writer.write("]]");
        
        atLineBeginning = false;
    }

    @Override
    public void internalLink(String id, String text) throws IOException {
        
        indentIfNecessary();
        writer.write('[');
        writer.write(id);
        writer.write(' ');
        writer.write(text);
        writer.write(']');
        
        atLineBeginning = false;
    }

    @Override
    public void externalLink(URI uri, String text) throws IOException {
        
        indentIfNecessary();
        writer.write('[');
        writer.write(uri.toString());
        writer.write(' ');
        writer.write(text);
        writer.write(']');
        
        atLineBeginning = false;
    }

    private void newLine() throws IOException {
        writer.write('\n');
        atLineBeginning = true;
    }
    
    private void indentIfNecessary() throws IOException {
        
        if (atLineBeginning && indentLevel > 0) {
            writer.write(StringUtils.repeat(';', indentLevel));
            writer.write(' ');
            
            atLineBeginning = false;
        }            
    }
    
    private String wrap(String inner, String outer) {
        return outer + inner + outer;
    }
}
