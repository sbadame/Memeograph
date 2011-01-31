package memeograph;

import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sandro Badame <a href="mailto:s.badame@gmail.com">s.badame&amp;gmail.com</a>
 */
public class ConfigTest {

  /**
   * Test of parseArgs method, of class Config.
   */
  @Test
  public void testParseArgs_CharSequence_HashMap() {
    String args = "";
    Map<String, String> result = Config.parseArgs(args);
    assertTrue(result.isEmpty());

    String args2 = "+hello=world";
    Map<String, String> result2 = Config.parseArgs(args2);
    assertTrue(result2.get("hello").equals("world"));

    String args5 = "+duh";
    Map<String, String> result5 = Config.parseArgs(args5);
    assertTrue(result5.get("+duh").equals("true"));


  }

  public static void main(String[] args) {
   new ConfigTest().testParseArgs_CharSequence_HashMap();
  }

}