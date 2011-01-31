package memeograph.generator.jdb.nodes;

import com.sun.jdi.Value;

/**
 * Instances of this class means that the code is missing some type or that
 * something went wrong... not good.
 */
class UnknownNode implements GraphNodeType{
  private static int count = 0;

  public final String cls;
  public final int id;

  public UnknownNode(Value val) {
    cls = val.getClass().getName();
    id = count++;
    System.err.println("Warning an unknown node has been created: " + this);
  }

  public String getUniqueID() {
    return "Unknown["+id+"]" + cls;
  }

}
