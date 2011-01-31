package memeograph.generator.jdb.nodes;

import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;

public class ObjectNode implements GraphNodeType{

  public final String name;
  public final String id;
  public final ObjectClassType type;

  public ObjectNode(ObjectReference thisObject) {
    name = thisObject.type().name() + "<" + thisObject.uniqueID() + ">";
    id = "Object[" + new Long(thisObject.uniqueID()) + "]";
    type = ObjectClassType.getObjectClassType((ClassType) thisObject.referenceType());
  }

  public String getUniqueID() {
    return id;
  }

  @Override
  public String toString(){
    return name;
  }
}
