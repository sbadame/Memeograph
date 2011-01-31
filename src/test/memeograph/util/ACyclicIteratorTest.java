/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memeograph.util;

import java.util.Arrays;
import java.util.Iterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sandro Badame <a href="mailto:s.badame@gmail.com">s.badame&amp;gmail.com</a>
 */
public class ACyclicIteratorTest {


  public static void main(String[] args) {
    ACyclicIteratorTest aCyclicIteratorTest = new ACyclicIteratorTest();
    aCyclicIteratorTest.testObjects();
  }


   @Test
   public void test(){
      ACyclicIterator i = new ACyclicIterator(Arrays.asList(1, 1, 1, 1, 2, 3, 4, 5, 5, 2, 3, 1, 6, 7).iterator());
      assertTrue(i.hasNext());
      assertEquals(1, i.next());
      assertEquals(2, i.next());
      assertEquals(3, i.next());
      assertEquals(4, i.next());
      assertEquals(5, i.next());
      assertEquals(6, i.next());
      assertEquals(7, i.next());
      assertFalse(i.hasNext());
   }

   @Test
   public void testObjects(){
      CustomObject c1 = new CustomObject(1, 1.5f);
      CustomObject c2 = new CustomObject( c1, 2, 3.5f);
      CustomObject c3 = new CustomObject( c2, 3, 5.5f);
      CustomObject c4 = new CustomObject( c3, 4, 5.5f);
      c1.next = c4;

      Iterator<CustomObject> iterator = new CustomObjectIterator(c1);
      ACyclicIterator i = new ACyclicIterator(iterator);

      assertEquals(c1, i.next());
      assertEquals(c4, i.next());
      assertEquals(c3, i.next());
      assertEquals(c2, i.next());
      assertFalse(i.hasNext());
   }

class CustomObjectIterator implements Iterator<CustomObject>{
    CustomObject current;

    public CustomObjectIterator(CustomObject c){current = c;}

    public boolean hasNext() { return current.next != null; }

    public CustomObject next() {
      CustomObject n = current;
      current = current.next;
      return n;
    }

    public void remove() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

}


class CustomObject {
       CustomObject next;
       int x;
       float y;

    public CustomObject(int x, float y) {
      this.x = x;
      this.y = y;
    }

    public CustomObject(CustomObject next, int x, float y) {
      this.next = next;
      this.x = x;
      this.y = y;
    }



      @Override
      public boolean equals(Object obj) {
        if (obj == null) {
          return false;
        }
        if (getClass() != obj.getClass()) {
          return false;
        }
        final CustomObject other = (CustomObject) obj;
        //if (this.next != other.next && (this.next == null || !this.next.equals(other.next))) {
        //  return false;
        //}
        if (this.x != other.x) {
          return false;
        }
        if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
          return false;
        }
        return true;
      }

      @Override
      public int hashCode() {
        int hash = 5;
        //hash = 89 * hash + (this.next != null ? this.next.hashCode() : 0);
        hash = 89 * hash + this.x;
        hash = 89 * hash + Float.floatToIntBits(this.y);
        return hash;
      }

     }
}