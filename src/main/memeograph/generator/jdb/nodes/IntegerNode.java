package memeograph.generator.jdb.nodes;

import com.sun.jdi.IntegerValue;

public class IntegerNode extends GraphNodeType{
  public int count = 0;

  public final int value;
  public final String uid;

  public IntegerNode(IntegerValue val){
     value = val.intValue();
     uid = "Integer["+ count++ + "]="+val;
  }

  public String getUniqueID() {
    return uid;
  }

  @Override
  public String toString(){
    return String.valueOf(value);
  }

}
