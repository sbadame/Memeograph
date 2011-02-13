package memeograph.generator.jdi.nodes;

import com.sun.jdi.ArrayReference;

public class ArrayNode extends GraphNodeType{


  final public String name;
  final public String uid;

  public ArrayNode(ArrayReference ar){
    String tmp =  ar.type().name();
    tmp = tmp.substring(0,tmp.length()-1) + ar.length() + "]";
    if (tmp.startsWith("java.lang."))tmp = tmp.substring("java.lang.".length());

    name = tmp;
    uid = new Long(ar.uniqueID()).toString();
  }

  public String getUniqueID() {
    return uid;
  }

  @Override
  public String toString(){
    return name;
  }

}
