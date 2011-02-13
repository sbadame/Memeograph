package memeograph.generator.jdi.nodes;

import com.sun.jdi.ClassType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ObjectClassType implements Serializable{

  //We need this cache to eliminate infinite loops and speed up the process
  //But it need not be stored in a graph file...
  private transient static HashMap<ClassType, ObjectClassType> cache = new HashMap<ClassType, ObjectClassType>();

  public static ObjectClassType getObjectClassType(ClassType t){
      if (cache.containsKey(t)) { return cache.get(t); }
      ObjectClassType oct = new ObjectClassType(t);
      return oct;
  }

  public static void clearCache(){
    cache.clear();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ObjectClassType other = (ObjectClassType) obj;
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
  }


  public final String name;
  public final ObjectClassType superclass;
  public final List<ObjectClassType> subclasses;

  private ObjectClassType(ClassType t){
    cache.put(t, this);

    name = t.name();
    if (t.superclass() != null) {
      superclass = getObjectClassType(t.superclass());
    }else{
      superclass = null;
    }

    subclasses = new ArrayList<ObjectClassType>();
    for (ClassType subclass : t.subclasses()) {
      subclasses.add(getObjectClassType(subclass));
    }
  }

  public boolean isSuperclassOf(ObjectClassType other){
    for (ObjectClassType subclass : subclasses) {
      if (other.name.equals(subclass.name) || subclass.isSuperclassOf(other)) { return true; }
    }
    return false;
  }

  public boolean isSubclassOf(ObjectClassType other){
    ObjectClassType sc = superclass;
    while(sc != null && !sc.name.equals("java.lang.Object")){
      if (sc.name.equals(other.name)) { return true; }
    }
    return false;
  }

}
