package memeograph.generator.jdb.nodes;

import com.sun.jdi.CharValue;

class CharNode implements GraphNodeType{
  private static int count = 0;

  public final char value;
  public final String uid;

  public CharNode(CharValue iv) {
    value = iv.charValue();
    uid = "Char[" + count++ + "]="+value;
  }

  public String getUniqueID() {
    return uid;
  }

  @Override
  public String toString(){
    return String.valueOf(value);
  }

}
