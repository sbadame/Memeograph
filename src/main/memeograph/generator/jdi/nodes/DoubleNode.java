package memeograph.generator.jdi.nodes;

import com.sun.jdi.DoubleValue;

class DoubleNode extends GraphNodeType{
  public int count = 0;

  public final double value;
  public final String uid;

  public DoubleNode(DoubleValue iv) {
    value = iv.doubleValue();
    uid = "Double["+ count++ + "]="+value;
  }

  public String getUniqueID() {
    return uid;
  }

  @Override
  public String toString(){
    return String.valueOf(value);
  }

}
