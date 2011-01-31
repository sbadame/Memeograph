/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memeograph.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sandro Badame <a href="mailto:s.badame@gmail.com">s.badame&amp;gmail.com</a>
 */
public class UtilTest {

  @Test
  public void testTrim(){
    assertTrue(Util.trim("Hello").toString().equals("Hello"));
    assertTrue(Util.trim("  World").toString().equals("World"));
    assertTrue(Util.trim("Dog  ").toString().equals("Dog"));
    assertTrue(Util.trim("      ").toString().equals(""));
    assertTrue(Util.trim("  hippo    ").toString().equals("hippo"));
    assertTrue(Util.trim("  hip  po    ").toString().equals("hip  po"));
    assertTrue(Util.trim("+hello=world").toString().equals("+hello=world"));
  }

}