package memeograph.generator.jdi.nodes;

import com.sun.jdi.FloatValue;

class FloatNode extends GraphNodeType {

  private static int count = 0;

  public final float value;
  public final String id;

  public FloatNode(FloatValue iv) {
    value = iv.floatValue();
    id = "Float[" + count++ + "]=" + value;
  }

  public String getUniqueID() { return id; }

  @Override
  public String toString(){
    return String.valueOf(value);
  }

}
