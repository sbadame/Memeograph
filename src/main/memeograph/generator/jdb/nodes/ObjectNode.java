package memeograph.generator.jdb.nodes;

import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import java.awt.Color;

public class ObjectNode extends GraphNodeType{

  public final String name;
  public final String id;
  public final ObjectClassType type;

  private Color c = null;

  public ObjectNode(ObjectReference thisObject) {
    name = thisObject.type().name() + "<" + thisObject.uniqueID() + ">";
    id = "Object[" + new Long(thisObject.uniqueID()) + "]";
    type = ObjectClassType.getObjectClassType((ClassType) thisObject.referenceType());

    for (Field f : thisObject.referenceType().allFields()) {
      if (f.name().equals("memeographcolor") && f.typeName().equals("int")) {
        IntegerValue v = (IntegerValue) thisObject.getValue(f);
        int rgb = v.value();
        c = new Color(rgb);
        break;
      }
    }
  }

  public String getUniqueID() {
    return id;
  }

  @Override
  public Color getColor()
  {
    return c;
  }

  @Override
  public String toString(){
    return name;
  }
}
