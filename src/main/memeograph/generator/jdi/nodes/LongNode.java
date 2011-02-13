package memeograph.generator.jdi.nodes;

import com.sun.jdi.LongValue;

class LongNode extends GraphNodeType{

  private static int count = 0;

  public final long value;
  public final String uid;
  public LongNode(LongValue iv) {
    value = iv.longValue();
    uid = "Long[" + count++ + "]=" + value;
  }

  public String getUniqueID() {
    return uid;
  }

  @Override
  public String toString(){
    return String.valueOf(value);
  }

}
