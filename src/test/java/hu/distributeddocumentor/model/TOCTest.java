package hu.distributeddocumentor.model;

import hu.distributeddocumentor.model.toc.DefaultTOCNode;
import hu.distributeddocumentor.model.toc.DefaultTOCNodeFactory;
import hu.distributeddocumentor.model.toc.TOC;
import org.junit.*;
import static org.junit.Assert.assertEquals;

public class TOCTest {
    
    public TOCTest() {
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

    /**
     * Test of addToEnd method, of class TOC.
     */
    @Test
    public void testAddToEnd() {
        
        TOC toc = new TOC(null, new DefaultTOCNodeFactory());
        
        assertEquals(2, toc.getRoot().getChildren().size());
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(0));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(1));
     
        DefaultTOCNode child1 = new DefaultTOCNode();
        toc.addToEnd(toc.getRoot(), child1);
        
        assertEquals(3, toc.getRoot().getChildren().size());
        assertEquals(child1, toc.getRoot().getChildren().get(0));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(1));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(2));
     
        DefaultTOCNode child2 = new DefaultTOCNode();
        toc.addToEnd(toc.getRoot(), child2);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(child1, toc.getRoot().getChildren().get(0));
        assertEquals(child2, toc.getRoot().getChildren().get(1));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(2));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(3));
        
        DefaultTOCNode child3 = new DefaultTOCNode();
        toc.addToEnd(child1, child3);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(1, child1.getChildren().size());
        assertEquals(child3, child1.getChildren().get(0));
    }

    /**
     * Test of addBefore method, of class TOC.
     */
    @Test
    public void testAddBefore() {
        
        TOC toc = new TOC(null, new DefaultTOCNodeFactory());
        
        DefaultTOCNode child1 = new DefaultTOCNode();
        toc.addBefore(toc.getUnorganized(), child1);
        
        assertEquals(3, toc.getRoot().getChildren().size());
        assertEquals(child1, toc.getRoot().getChildren().get(0));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(1));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(2));
     
        DefaultTOCNode child2 = new DefaultTOCNode();
        toc.addBefore(toc.getRecycleBin(), child2);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(child1, toc.getRoot().getChildren().get(0));
        assertEquals(child2, toc.getRoot().getChildren().get(1));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(2));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(3));
        
        DefaultTOCNode child3 = new DefaultTOCNode();
        toc.addBefore(child1, child3);
        
        assertEquals(5, toc.getRoot().getChildren().size());
        assertEquals(child3, toc.getRoot().getChildren().get(0));
        assertEquals(child1, toc.getRoot().getChildren().get(1));
        assertEquals(child2, toc.getRoot().getChildren().get(2));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(3));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(4));
    }

    /**
     * Test of addAfter method, of class TOC.
     */
    @Test
    public void testAddAfter() {
        TOC toc = new TOC(null, new DefaultTOCNodeFactory());
        
        DefaultTOCNode child1 = new DefaultTOCNode();
        toc.addAfter(toc.getUnorganized(), child1);
        
        assertEquals(3, toc.getRoot().getChildren().size());
        assertEquals(child1, toc.getRoot().getChildren().get(0));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(1));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(2));
     
        DefaultTOCNode child2 = new DefaultTOCNode();
        toc.addAfter(toc.getRecycleBin(), child2);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(child1, toc.getRoot().getChildren().get(0));
        assertEquals(child2, toc.getRoot().getChildren().get(1));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(2));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(3));
        
        DefaultTOCNode child3 = new DefaultTOCNode();
        toc.addAfter(child1, child3);
        
        assertEquals(5, toc.getRoot().getChildren().size());
        assertEquals(child1, toc.getRoot().getChildren().get(0));
        assertEquals(child3, toc.getRoot().getChildren().get(1));
        assertEquals(child2, toc.getRoot().getChildren().get(2));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(3));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(4));
        
        DefaultTOCNode child4 = new DefaultTOCNode();
        toc.addAfter(child2, child4);
        
        assertEquals(6, toc.getRoot().getChildren().size());
        assertEquals(child1, toc.getRoot().getChildren().get(0));
        assertEquals(child3, toc.getRoot().getChildren().get(1));
        assertEquals(child2, toc.getRoot().getChildren().get(2));
        assertEquals(child4, toc.getRoot().getChildren().get(3));
        assertEquals(toc.getUnorganized(), toc.getRoot().getChildren().get(4));
        assertEquals(toc.getRecycleBin(), toc.getRoot().getChildren().get(5));
    }

    /**
     * Test of moveUp method, of class TOC.
     */
    @Test
    public void testMoveUp() {
        
        TOC toc = new TOC(null, new DefaultTOCNodeFactory());
        
        // Initial structure:
        // node1
        //   node11
        // node2
        //   node21
        //   node22
        //   node23
        //     node231
        
        DefaultTOCNode node1 = new DefaultTOCNode("1");
        DefaultTOCNode node11 = new DefaultTOCNode("1.1");
        DefaultTOCNode node2 = new DefaultTOCNode("2");
        DefaultTOCNode node21 = new DefaultTOCNode("2.1");
        DefaultTOCNode node22 = new DefaultTOCNode("2.2");
        DefaultTOCNode node23 = new DefaultTOCNode("2.3");
        DefaultTOCNode node231 = new DefaultTOCNode("2.3.1");
        
        toc.addToEnd(toc.getRoot(), node1);
        toc.addToEnd(toc.getRoot(), node2);
        toc.addToEnd(node1, node11);
        toc.addToEnd(node2, node21);
        toc.addToEnd(node2, node22);
        toc.addToEnd(node2, node23);
        toc.addToEnd(node23, node231);
        
        // moving up node231, step 1:
        toc.moveUp(node231);
        
        // expected:
        // node1
        //   node11
        // node2
        //   node21
        //   node22
        //   node231
        //   node23        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));        
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));        
                
        assertEquals(4, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));        
        assertEquals(node22, node2.getChildren().get(1));        
        assertEquals(node231, node2.getChildren().get(2));        
        assertEquals(node23, node2.getChildren().get(3));        
        
        // moving up node231, step 2:
        toc.moveUp(node231);
        
        // expected:
        // node1
        //   node11
        // node2
        //   node21
        //   node231
        //   node22
        //   node23        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));        
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));        
                
        assertEquals(4, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));        
        assertEquals(node231, node2.getChildren().get(1));        
        assertEquals(node22, node2.getChildren().get(2));        
        assertEquals(node23, node2.getChildren().get(3));    
        
        // moving up node231, step 3:
        toc.moveUp(node231);
        
        // expected:
        // node1
        //   node11
        // node2
        //   node231
        //   node21
        //   node22
        //   node23        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));        
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));        
                
        assertEquals(4, node2.getChildren().size());
        assertEquals(node231, node2.getChildren().get(0));        
        assertEquals(node21, node2.getChildren().get(1));        
        assertEquals(node22, node2.getChildren().get(2));        
        assertEquals(node23, node2.getChildren().get(3));   
        
        // moving up node231, step 4:
        toc.moveUp(node231);
        
        // expected:
        // node1
        //   node11
        // node231
        // node2
        //   node21
        //   node22
        //   node23        
        assertEquals(5, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));        
        assertEquals(node231, toc.getRoot().getChildren().get(1));
        assertEquals(node2, toc.getRoot().getChildren().get(2));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));        
                
        assertEquals(3, node2.getChildren().size());   
        assertEquals(node21, node2.getChildren().get(0));        
        assertEquals(node22, node2.getChildren().get(1));        
        assertEquals(node23, node2.getChildren().get(2));  
        
        // moving up node231, step 5:
        toc.moveUp(node231);
        
        // expected:
        // node231
        // node1
        //   node11
        // node2
        //   node21
        //   node22
        //   node23        
        assertEquals(5, toc.getRoot().getChildren().size());
        assertEquals(node231, toc.getRoot().getChildren().get(0));        
        assertEquals(node1, toc.getRoot().getChildren().get(1));
        assertEquals(node2, toc.getRoot().getChildren().get(2));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));        
                
        assertEquals(3, node2.getChildren().size());   
        assertEquals(node21, node2.getChildren().get(0));        
        assertEquals(node22, node2.getChildren().get(1));        
        assertEquals(node23, node2.getChildren().get(2));  
        
        // moving up node231, step 6:
        toc.moveUp(node231);
        
        // expected:
        // node231
        // node1
        //   node11
        // node2
        //   node21
        //   node22
        //   node23        
        assertEquals(5, toc.getRoot().getChildren().size());
        assertEquals(node231, toc.getRoot().getChildren().get(0));        
        assertEquals(node1, toc.getRoot().getChildren().get(1));
        assertEquals(node2, toc.getRoot().getChildren().get(2));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));        
                
        assertEquals(3, node2.getChildren().size());   
        assertEquals(node21, node2.getChildren().get(0));        
        assertEquals(node22, node2.getChildren().get(1));        
        assertEquals(node23, node2.getChildren().get(2));  
    }

    /**
     * Test of moveDown method, of class TOC.
     */
    @Test
    public void testMoveDown() {
            
        TOC toc = new TOC(null, new DefaultTOCNodeFactory());
        
        // Initial structure:
        // node1
        //   node11
        //     node111
        //     node112
        // node2
        //   node21        
        
        DefaultTOCNode node1 = new DefaultTOCNode("1");
        DefaultTOCNode node11 = new DefaultTOCNode("1.1");
        DefaultTOCNode node111 = new DefaultTOCNode("1.1.1");
        DefaultTOCNode node112 = new DefaultTOCNode("1.1.2");
        DefaultTOCNode node2 = new DefaultTOCNode("2");
        DefaultTOCNode node21 = new DefaultTOCNode("2.1");
        
        toc.addToEnd(toc.getRoot(), node1);
        toc.addToEnd(toc.getRoot(), node2);
        toc.addToEnd(node1, node11);
        toc.addToEnd(node11, node111);
        toc.addToEnd(node11, node112);
        toc.addToEnd(node2, node21);
        
        // Moving doen node111, step 1
        // expected:
        // node1
        //    node11
        //       node112
        //       node111
        // node2
        //   node21
        
        toc.moveDown(node111);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));
        
        assertEquals(2, node11.getChildren().size());
        assertEquals(node112, node11.getChildren().get(0));
        assertEquals(node111, node11.getChildren().get(1));
        
        assertEquals(1, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));
        
        // Moving doen node111, step 2
        // expected:
        // node1
        //    node11
        //       node112
        //    node111
        // node2
        //   node21
        
        toc.moveDown(node111);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        
        assertEquals(2, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));
        assertEquals(node111, node1.getChildren().get(1));
        
        assertEquals(1, node11.getChildren().size());
        assertEquals(node112, node11.getChildren().get(0));        
        
        assertEquals(1, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));   
        
        // Moving doen node111, step 3
        // expected:
        // node1
        //    node11
        //       node112
        // node111
        // node2
        //   node21
        
        toc.moveDown(node111);
        
        assertEquals(5, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node111, toc.getRoot().getChildren().get(1));
        assertEquals(node2, toc.getRoot().getChildren().get(2));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));
        
        assertEquals(1, node11.getChildren().size());
        assertEquals(node112, node11.getChildren().get(0));        
        
        assertEquals(1, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));   
        
        // Moving doen node111, step 4
        // expected:
        // node1
        //    node11
        //       node112       
        // node2
        //   node21
        // node111
        
        toc.moveDown(node111);
        
        assertEquals(5, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        assertEquals(node111, toc.getRoot().getChildren().get(2));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));
        
        assertEquals(1, node11.getChildren().size());
        assertEquals(node112, node11.getChildren().get(0));        
        
        assertEquals(1, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));   
        
        // Moving doen node111, step 5
        // expected:
        // node1
        //    node11
        //       node112       
        // node2
        //   node21
        // node111
        
        toc.moveDown(node111);
        
        assertEquals(5, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        assertEquals(node111, toc.getRoot().getChildren().get(2));
        
        assertEquals(1, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));
        
        assertEquals(1, node11.getChildren().size());
        assertEquals(node112, node11.getChildren().get(0));        
        
        assertEquals(1, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));   
    }
    
    @Test
    public void testMoveLeft() {
        
        TOC toc = new TOC(null, new DefaultTOCNodeFactory());
        
        // Initial structure:
        // node1
        //   node11
        //     node111
        //     node112
        // node2
        //   node21        
        
        DefaultTOCNode node1 = new DefaultTOCNode("1");
        DefaultTOCNode node11 = new DefaultTOCNode("1.1");
        DefaultTOCNode node111 = new DefaultTOCNode("1.1.1");
        DefaultTOCNode node112 = new DefaultTOCNode("1.1.2");
        DefaultTOCNode node2 = new DefaultTOCNode("2");
        DefaultTOCNode node21 = new DefaultTOCNode("2.1");
        
        toc.addToEnd(toc.getRoot(), node1);
        toc.addToEnd(toc.getRoot(), node2);
        toc.addToEnd(node1, node11);
        toc.addToEnd(node11, node111);
        toc.addToEnd(node11, node112);
        toc.addToEnd(node2, node21);
        
     
        // moving left node111
        // expected:
        // node1
        //   node111
        //   node11        
        //     node112
        // node2
        //   node21 
        
        toc.moveLeft(node111);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        assertEquals(2, node1.getChildren().size());
        assertEquals(node111, node1.getChildren().get(0));
        assertEquals(node11, node1.getChildren().get(1));
        assertEquals(1, node11.getChildren().size());
        assertEquals(node112, node11.getChildren().get(0));    
        
        // moving left node112
        // expected:
        // node1
        //   node111
        //   node112
        //   node11   
        // node2
        //   node21 
        
        toc.moveLeft(node112);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        assertEquals(3, node1.getChildren().size());
        assertEquals(node111, node1.getChildren().get(0));
        assertEquals(node112, node1.getChildren().get(1));
        assertEquals(node11, node1.getChildren().get(2));
        assertEquals(0, node11.getChildren().size());     
        
        // moving left nod1 and node2: nothing happens
        toc.moveLeft(node1);
        toc.moveLeft(node2);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        assertEquals(3, node1.getChildren().size());
        assertEquals(node111, node1.getChildren().get(0));
        assertEquals(node112, node1.getChildren().get(1));
        assertEquals(node11, node1.getChildren().get(2));
        assertEquals(0, node11.getChildren().size());  
    }
    
    @Test
    public void testMoveRight() {
        TOC toc = new TOC(null, new DefaultTOCNodeFactory());
        
        // Initial structure:
        // node1
        //   node11
        //     node111
        //     node112
        // node2
        //   node21        
        
        DefaultTOCNode node1 = new DefaultTOCNode("1");
        DefaultTOCNode node11 = new DefaultTOCNode("1.1");
        DefaultTOCNode node111 = new DefaultTOCNode("1.1.1");
        DefaultTOCNode node112 = new DefaultTOCNode("1.1.2");
        DefaultTOCNode node2 = new DefaultTOCNode("2");
        DefaultTOCNode node21 = new DefaultTOCNode("2.1");
        
        toc.addToEnd(toc.getRoot(), node1);
        toc.addToEnd(toc.getRoot(), node2);
        toc.addToEnd(node1, node11);
        toc.addToEnd(node11, node111);
        toc.addToEnd(node11, node112);
        toc.addToEnd(node2, node21);
        
        // moving right node1: nothing happens
        
        toc.moveRight(node1);
        
        assertEquals(4, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(node2, toc.getRoot().getChildren().get(1));
        assertEquals(1, node1.getChildren().size());
        
        // moving right node2:
        // expected:
        // node1
        //   node11
        //     node111
        //     node112
        //   node2
        //     node21    
        
        toc.moveRight(node2);
        
        assertEquals(3, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(2, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));
        assertEquals(node2, node1.getChildren().get(1));
        assertEquals(1, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));
        assertEquals(2, node11.getChildren().size());
        
        // moving right node11, node111 and node21: nothing happens
        toc.moveRight(node11);
        toc.moveRight(node111);
        toc.moveRight(node21);
                
        assertEquals(3, toc.getRoot().getChildren().size());
        assertEquals(node1, toc.getRoot().getChildren().get(0));
        assertEquals(2, node1.getChildren().size());
        assertEquals(node11, node1.getChildren().get(0));
        assertEquals(node2, node1.getChildren().get(1));
        assertEquals(1, node2.getChildren().size());
        assertEquals(node21, node2.getChildren().get(0));
        assertEquals(2, node11.getChildren().size());
    }
}
