package memeograph.generator.jdi.nodes;

import com.sun.jdi.ShortValue;

class ShortNode extends GraphNodeType{
  private static int count = 0;

  public final short value;
  public final String uid;

  public ShortNode(ShortValue iv) {
    value = iv.shortValue();
    uid = "Short[" + count++ + "]=" + value;
  }

  public String getUniqueID() {
    return uid;
  }

  @Override
  public String toString(){
    return String.valueOf(value);
  }

}
