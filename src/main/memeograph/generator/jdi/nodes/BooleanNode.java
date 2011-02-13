package memeograph.generator.jdi.nodes;

import com.sun.jdi.BooleanValue;

class BooleanNode extends GraphNodeType {

  private static int count = 0;

  public final boolean value;
  public final String id;

  public BooleanNode(BooleanValue iv) {
    value = iv.booleanValue();
    id = "Boolean[" + count++ + "]=" + (value ? "true" : "false");
  }

  public String getUniqueID() {
    return id;
  }

  @Override
  public String toString(){
    return String.valueOf(value);
  }


}
