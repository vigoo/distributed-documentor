/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.distributeddocumentor.utils;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author vigoo
 */
public class RepositoryUriGeneratorTest {
    
    public RepositoryUriGeneratorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testNoCredentials() {
        
        String result = RepositoryUriGenerator.addCredentials("http://x.y.z", null, null);        
        assertEquals("http://x.y.z", result);
        
        result = RepositoryUriGenerator.addCredentials("http://x.y.z", "", "");        
        assertEquals("http://x.y.z", result);
    }
    
    /**
     * Test of addCredentials method, of class RepositoryUriGenerator.
     */
    @Test
    public void testAddCredentials() {
        
        String result = RepositoryUriGenerator.addCredentials("http://x.y.z", "vigoo", "alma");        
        assertEquals("http://vigoo:alma@x.y.z", result);
        
        result = RepositoryUriGenerator.addCredentials("http://x.y.z/a/b/c", "vigoo", "alma");        
        assertEquals("http://vigoo:alma@x.y.z/a/b/c", result);
        
        result = RepositoryUriGenerator.addCredentials("http://x.y.z/a/b/c?var=val", "vigoo", "alma");        
        assertEquals("http://vigoo:alma@x.y.z/a/b/c?var=val", result);
        
        result = RepositoryUriGenerator.addCredentials("http://x.y.z?var=val", "vigoo", "alma");        
        assertEquals("http://vigoo:alma@x.y.z?var=val", result);
        
        result = RepositoryUriGenerator.addCredentials("http://x.y.z:888?var=val", "vigoo", "alma");        
        assertEquals("http://vigoo:alma@x.y.z:888?var=val", result);
        
        result = RepositoryUriGenerator.addCredentials("https://x.y.z/x", "vigoo", "alma");        
        assertEquals("https://vigoo:alma@x.y.z/x", result);
        
        result = RepositoryUriGenerator.addCredentials("x.y.z/a/b/c", "vigoo", "alma");        
        assertEquals("http://vigoo:alma@x.y.z/a/b/c", result);
    }
}
