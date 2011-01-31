/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memeograph.util;

import java.util.ArrayList;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Sandro Badame <a href="mailto:s.badame@gmail.com">s.badame&amp;gmail.com</a>
 */
public class CompoundIteratorTest  {

  @Test
  public void test1(){
    ArrayList<Integer> a1 = new ArrayList<Integer>();
    for(int i = 0; i<5; i+=1){a1.add(i);}
    ArrayList<Integer> a2 = new ArrayList<Integer>();
    for(int i = 5; i<10; i+=1){a2.add(i);}
    CompoundIterator<Integer> compoundIterator = new CompoundIterator<Integer>(a1.iterator(), a2.iterator());

    assertTrue(compoundIterator.hasNext());
    for(Integer i = 0; i < 10; i++){assertEquals(i, compoundIterator.next());}
    assertFalse(compoundIterator.hasNext());
  }


  @Test
  public void test2(){
    ArrayList<String> a1 = new ArrayList<String>();
    ArrayList<String> a2 = new ArrayList<String>();
    ArrayList<String> a3 = new ArrayList<String>();

    CompoundIterator<String> ci = new CompoundIterator<String>(a1.iterator(), a2.iterator(), a3.iterator());
    assertFalse(ci.hasNext());
  }
}