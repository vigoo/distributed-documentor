package hu.distributeddocumentor.model;

import org.junit.*;
import static org.junit.Assert.assertEquals;

public class PageTest {

    @Test
    public void testModifyPageReferences() {
        
        Page p = new Page("test", null);
        p.setMarkupLanguage("MediaWiki");
        p.setMarkup("[[FirstLink]]\n[[SecondLink|the second link]]\n[[ThirdLink]]\n[[ThirdLink|third link again]]\n[[FourthLink]]");

        p.modifyPageReferences("ThirdLink", "THL");
        assertEquals("[[FirstLink]]\n[[SecondLink|the second link]]\n[[THL]]\n[[THL|third link again]]\n[[FourthLink]]", p.getMarkup());
        p.modifyPageReferences("secondlink", "SL");
        assertEquals("[[FirstLink]]\n[[SL|the second link]]\n[[THL]]\n[[THL|third link again]]\n[[FourthLink]]", p.getMarkup());
    }
}
