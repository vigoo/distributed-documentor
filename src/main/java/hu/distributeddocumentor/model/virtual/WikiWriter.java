package hu.distributeddocumentor.model.virtual;

import java.io.IOException;
import java.net.URI;


public interface WikiWriter {

    void heading(int level, String title) throws IOException;
    
    void newParagraph() throws IOException;
    
    void beginIndent(int level);
    void endIndent();
    
    void beginBullet(int level) throws IOException;
    void beginEnumerationItem(int level) throws IOException;
    
    void sourceCode(String lang, String code) throws IOException;
    
    void text(String text) throws IOException;
    void bold(String text) throws IOException;
    void italic(String text) throws IOException;
    void boldItalic(String text) throws IOException;
    void image(String name) throws IOException;
    void internalLink(String id, String text) throws IOException;
    void externalLink(URI uri, String text) throws IOException;
}
