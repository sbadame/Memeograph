package memeograph.graph;

import memeograph.graph.MutableNode;
import memeograph.graph.Node;

/**
 * Warning!! This does NOT support cycles. The toString() method WILL overflow.
 */
public class SimpleNode extends MutableNode {

  public SimpleNode(Object data){
    super();
    hashMap.put(data.getClass(), data);
  }

  public SimpleNode(Object data, Node... children){
    this(data);
    for (Node child : children) { addChild(child); }
  }

  @Override
  public String toString(){
    StringBuilder b = new StringBuilder(lookup(String.class));
    b.append("{");
    if (hasChildren()) {
      for (Node node : children) b.append(node.toString()).append(",");
      b.setLength(b.length()-1);
    }
    b.append("}");
    return b.toString();
  }

}
