package memeograph.generator.jdb.nodes;

import com.sun.jdi.ThreadReference;

public class ThreadNode extends GraphNodeType {

  public final long uid;
  public final String name;

  public ThreadNode(ThreadReference t){
    uid = t.uniqueID();
    name = t.name();
  }

  public String getUniqueID() {
    return "Thread[" + uid + "]=" + name;
  }

  @Override
  public String toString(){
    return "Thread: " + name;
  }
}
