package hu.distributeddocumentor.model.virtual;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class MediaWikiWriterTest {

    private StringWriter stringWriter;
    private MediaWikiWriter wikiWriter;
    
    @Before
    public void init() {
        stringWriter = new StringWriter();
        wikiWriter = new MediaWikiWriter(stringWriter);
    }
    
    private void reset() {
        init();
    }
    
    private String getResult() {
        stringWriter.flush();
        return stringWriter.toString();
    }
    
    @Test
    public void renderingSimpleHeadings() throws IOException {
        
        wikiWriter.heading(1, "Test");
        assertEquals("= Test =\n", getResult());
        
        reset();
        wikiWriter.heading(2, "Test");
        assertEquals("== Test ==\n", getResult());
        
        reset();
        wikiWriter.heading(3, "Test");
        assertEquals("=== Test ===\n", getResult());
        
        reset();
        wikiWriter.heading(4, "Test");
        assertEquals("==== Test ====\n", getResult());
        
        reset();
        wikiWriter.heading(5, "Test");
        assertEquals("===== Test =====\n", getResult());
        
        reset();
        wikiWriter.heading(6, "Test");
        assertEquals("====== Test ======\n", getResult());
    }
    
    @Test
    public void renderingSimpleTextParagraph() throws IOException {
        
        wikiWriter.text("hello world");        
        wikiWriter.newParagraph();
        
        assertEquals("hello world\n\n", getResult());
    }
    
    @Test
    public void renderingParagraphWithLineBreaks() throws IOException {
        
        wikiWriter.text("hello world\n");
        wikiWriter.text("second line");
        wikiWriter.newParagraph();
        
        assertEquals("hello world\nsecond line\n\n", getResult());
    }
    
    @Test
    public void renderingParagraphWithBoldAndItalic() throws IOException {
        
        wikiWriter.text("hello ");
        wikiWriter.italic("unit testing");
        wikiWriter.text(" ");
        wikiWriter.bold("world");
        wikiWriter.text("!!!");
        wikiWriter.newParagraph();
        
        assertEquals("hello ''unit testing'' '''world'''!!!\n\n", getResult());
    }
    
    @Test
    public void renderingIndentedMultiLineParagraph() throws IOException {
        
        wikiWriter.beginIndent(1);
        wikiWriter.text("hello world\n");
        wikiWriter.text("second line\n");
        wikiWriter.endIndent();
        wikiWriter.newParagraph();
        
        assertEquals("; hello world\n; second line\n\n", getResult());
        
        reset();
        wikiWriter.beginIndent(3);
        wikiWriter.text("hello world\n");
        wikiWriter.text("second line\n");
        wikiWriter.endIndent();
        wikiWriter.newParagraph();
        
        assertEquals(";;; hello world\n;;; second line\n\n", getResult());
    }
    
    @Test
    public void renderingBulletedList() throws IOException {
        
        renderBulletedTestList(1);
        assertEquals("* hello world\n* second line\n\n", getResult());
        
        renderBulletedTestList(2);
        assertEquals("** hello world\n** second line\n\n", getResult());
        
        renderBulletedTestList(3);
        assertEquals("*** hello world\n*** second line\n\n", getResult());
        
        renderBulletedTestList(4);
        assertEquals("**** hello world\n**** second line\n\n", getResult());        
    }
    
    private void renderBulletedTestList(int level) throws IOException {
        
        reset();
        wikiWriter.beginBullet(level);
        wikiWriter.text("hello world\n");
        wikiWriter.beginBullet(level);
        wikiWriter.text("second line\n");
        wikiWriter.newParagraph();
    }
    
    @Test
    public void renderingEnumeratedList() throws IOException {
        
        renderEnumeratedTestList(1);
        assertEquals("# hello world\n# second line\n\n", getResult());
        
        renderEnumeratedTestList(2);
        assertEquals("## hello world\n## second line\n\n", getResult());
        
        renderEnumeratedTestList(3);
        assertEquals("### hello world\n### second line\n\n", getResult());
        
        renderEnumeratedTestList(4);
        assertEquals("#### hello world\n#### second line\n\n", getResult());        
    }
    
    private void renderEnumeratedTestList(int level) throws IOException {
        
        reset();
        wikiWriter.beginEnumerationItem(level);
        wikiWriter.text("hello world\n");
        wikiWriter.beginEnumerationItem(level);
        wikiWriter.text("second line\n");
        wikiWriter.newParagraph();
    }
    
    @Test
    public void renderingJavaSourceCode() throws IOException {
        
        wikiWriter.sourceCode("java", "public static int func() {\n\treturn 0;\n}");
        wikiWriter.newParagraph();
        
        assertEquals("<pre class='brush: java'>\npublic static int func() {\n\treturn 0;\n}\n</pre>\n\n", getResult());
    }
    
    @Test
    public void renderingImage() throws IOException {
        
        wikiWriter.image("img01");
        assertEquals("[[Image:img01]]", getResult());
    }
    
    @Test
    public void renderingInternalLink() throws IOException {
        
        wikiWriter.internalLink("OtherPage", "something else");
        assertEquals("[OtherPage something else]", getResult());
    }
        
    @Test
    public void renderingExternalLink() throws IOException {
        
        wikiWriter.externalLink(URI.create("http://www.google.hu"), "Google");
        assertEquals("[http://www.google.hu Google]", getResult());
    }
}
