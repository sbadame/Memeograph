package memeograph.graph;

import memeograph.util.Filter;
import org.junit.Test;
import static org.junit.Assert.*;

public class GraphIteratorTest {

  @Test
  public void test(){
    SimpleNode eight = new SimpleNode(8);
    GraphIterator traversal = new GraphIterator(
      new SimpleNode(1,
          new SimpleNode( 2,
            new SimpleNode(3),
            new SimpleNode(4),
            new SimpleNode(5,
              new SimpleNode(6),
              new SimpleNode(7),
              new SimpleNode(9,
                new SimpleNode(10)
              ),
              eight
            )
          )
    ));
    assertTrue(traversal.hasNext());
    traversal.next();
    traversal.next();
    traversal.next();
    traversal.next();
    traversal.next();
    traversal.next();
    traversal.next();
    traversal.next();
    traversal.next();
    assertEquals(eight, traversal.next());
    assertFalse(traversal.hasNext());
  }

  @Test
  public void testfilter(){
/*
    Filter<Node> f= new Filter<Node>() {
      @Override
      public Boolean filter(Node from) {
        return from.lookup(Integer.class) % 2 == 0;
      }
    };

    GraphIterator traversal = new GraphIterator(
      new SimpleNode(1,
          new SimpleNode( 2,
            new SimpleNode(3),
            new SimpleNode(4),
            new SimpleNode(5,
              new SimpleNode(6),
              new SimpleNode(7),
              new SimpleNode(9,
                new SimpleNode(10)
              ),
              new SimpleNode(8)
            )
          )
    ), f);

    assertTrue(traversal.hasNext());
    assertEquals(new Integer(2), traversal.next().lookup(Integer.class));
    assertEquals(new Integer(4), traversal.next().lookup(Integer.class));
    assertEquals(new Integer(6), traversal.next().lookup(Integer.class));
    assertEquals(new Integer(10), traversal.next().lookup(Integer.class));
    assertEquals(new Integer(8), traversal.next().lookup(Integer.class));
    assertFalse(traversal.hasNext());
 
 */
  }

}
