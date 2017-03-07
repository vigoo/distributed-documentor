package hu.distributeddocumentor.model;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.assertEquals;

public class PageTest {

    @Test
    public void testModifyPageReferences() {
        
        Page p = new Page("test", null, new Conditions(), new File("custom.css"));
        p.setMarkupLanguage("MediaWiki");
        p.setMarkup("[[FirstLink]]\n[[SecondLink|the second link]]\n[[ThirdLink]]\n[[ThirdLink|third link again]]\n[[FourthLink]]");

        p.modifyPageReferences("ThirdLink", "THL");
        assertEquals("[[FirstLink]]\n[[SecondLink|the second link]]\n[[THL]]\n[[THL|third link again]]\n[[FourthLink]]", p.getMarkup());
        p.modifyPageReferences("secondlink", "SL");
        assertEquals("[[FirstLink]]\n[[SL|the second link]]\n[[THL]]\n[[THL|third link again]]\n[[FourthLink]]", p.getMarkup());
    }
    
    @Test
    public void simpleConditionals() {
        Conditions conditions = new Conditions();
        Page p = new Page("test", null, conditions, new File("custom.css"));
        
        p.setMarkup("First line\n\n[When:TEST]\nConditional line\n\n[End]\nLast line");
        String html = p.asHTML("/");
        assertEquals("<?xml version='1.0' encoding='utf-8' ?><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><link type=\"text/css\" rel=\"stylesheet\" href=\"/documentation.css\"/></head><body><p>First line</p><p>Last line</p></body></html>", html);
        
        conditions.enable("TEST");

        String html2 = p.asHTML("/");
        assertEquals("<?xml version='1.0' encoding='utf-8' ?><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><link type=\"text/css\" rel=\"stylesheet\" href=\"/documentation.css\"/></head><body><p>First line</p><p>Conditional line</p><p>Last line</p></body></html>", html2);
    }
    
    @Test
    public void nestedConditionals() {
        Conditions conditions = new Conditions();
        Page p = new Page("test", null, conditions, new File("custom.css"));
        
        p.setMarkup("First line\n\n[When:OUTER]\nOuter conditional line\n\n[When:INNER]\nInner conditional line\n\n[End]\n\n[End]\nLast line");
        String html = p.asHTML("/");
        assertEquals("<?xml version='1.0' encoding='utf-8' ?><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><link type=\"text/css\" rel=\"stylesheet\" href=\"/documentation.css\"/></head><body><p>First line</p><p>Last line</p></body></html>", html);
        
        conditions.enable("OUTER");

        String html2 = p.asHTML("/");
        assertEquals("<?xml version='1.0' encoding='utf-8' ?><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><link type=\"text/css\" rel=\"stylesheet\" href=\"/documentation.css\"/></head><body><p>First line</p><p>Outer conditional line</p><p>Last line</p></body></html>", html2);        
        
        conditions.enable("INNER");
        
        String html3 = p.asHTML("/");
        assertEquals("<?xml version='1.0' encoding='utf-8' ?><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><link type=\"text/css\" rel=\"stylesheet\" href=\"/documentation.css\"/></head><body><p>First line</p><p>Outer conditional line</p><p>Inner conditional line</p><p>Last line</p></body></html>", html3);                
    }
}
