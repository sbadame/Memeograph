package memeograph.generator.jdb.nodes;

import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;
import java.awt.Color;

public class ObjectNode extends GraphNodeType{

  public String name;
  public final String id;
  public final ObjectClassType type;

  public Color color = null;

  public ObjectNode(ObjectReference thisObject) {
    name = thisObject.type().name() + "<" + thisObject.uniqueID() + ">";
    id = "Object[" + new Long(thisObject.uniqueID()) + "]";
    type = ObjectClassType.getObjectClassType((ClassType) thisObject.referenceType());

  }

  public String getUniqueID() {
    return id;
  }

  @Override
  public Color getColor()
  {
    return color;
  }

  @Override
  public String toString(){
    return name;
  }
}
