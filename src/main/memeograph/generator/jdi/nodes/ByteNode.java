package memeograph.generator.jdi.nodes;

import com.sun.jdi.ByteValue;

class ByteNode extends GraphNodeType{
  public int count = 0;

  public final byte value;
  public final String id;

  public ByteNode(ByteValue iv) {
    value = iv.byteValue();
    id = "Byte[" + count++ + "]=" + value;
  }

  public String getUniqueID() {
    return id;
  }

  @Override
  public String toString(){
    return String.valueOf(value);
  }

}
