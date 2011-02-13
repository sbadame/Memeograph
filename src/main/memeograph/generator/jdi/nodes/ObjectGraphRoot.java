package memeograph.generator.jdi.nodes;

public class ObjectGraphRoot extends GraphNodeType{

  private static int counter = 0;

  private int id = counter++;

  public String getUniqueID() {
    return "ObjectGraphRoot[" + id + "]";
  }

}
