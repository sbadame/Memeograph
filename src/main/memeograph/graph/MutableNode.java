package memeograph.graph;

import java.util.HashMap;

/**
 * A Node that allows for children to be added to it.
 */
public class MutableNode extends Node{
  protected int childcount = 0;
  protected transient HashMap<Class, Object> hashMap = new HashMap<Class, Object>();

  /*
   * Adds a child to this node. Null children will be ignored.
   */
  public void addChild(Node n){
    if (n == null) {
      throw new NullPointerException();
    }
    childcount++;
    children.add(n);
  }

  public int getChildCount(){
    return childcount;
  }
}
