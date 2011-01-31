package memeograph.generator.jdb.nodes;

public class ObjectGraphRoot implements GraphNodeType{

  private static int counter = 0;

  private int id = counter++;

  public String getUniqueID() {
    return "ObjectGraphRoot[" + id + "]";
  }

}
