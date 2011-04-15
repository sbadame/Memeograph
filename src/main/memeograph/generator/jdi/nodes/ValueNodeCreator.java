package memeograph.generator.jdi.nodes;

import com.sun.jdi.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.regex.Pattern;
import memeograph.Config;
import memeograph.graph.MutableNode;

public class ValueNodeCreator {

  private HashMap<Value, MutableNode> valueCache = new HashMap<Value, MutableNode>();

  public void clear() {
    valueCache.clear();
  }
  
  public MutableNode getNode(Value val){
    if (valueCache.containsKey(val)) { return valueCache.get(val); }

    MutableNode n;

    if (val instanceof ArrayReference) {
      n = getArrayNode((ArrayReference)val);
      valueCache.put(val, n);
    }else if (val instanceof ObjectReference){
      n = getObjectReference((ObjectReference)val);
      valueCache.put(val, n);
    }else{
      n = getPrimitiveReference(val);
      if (Config.getConfig().isSwitchSet(Config.GROUP_PRIMATIVES, false)) {
        valueCache.put(val, n);
      }
    }

    return n;
  }

  private MutableNode getArrayNode(ArrayReference arrayReference) {
    MutableNode n = new MutableNode();
    n.gnt = new ArrayNode(arrayReference);

    for (Value value : arrayReference.getValues()) {
      if (value == null) { continue; }
      n.addChild(getNode(value));
    }

    return n;
  }

  private MutableNode getObjectReference(ObjectReference or) {

    MutableNode n = new MutableNode();
    ObjectNode on = new ObjectNode(or);
    n.gnt = on;

    //Fields
    ClassType type = (ClassType)or.type();
    for (Field field : type.allFields()) {
      Value val = or.getValue(field);

      //Null or refering to self, just ignore it
      if (val == null || val == or) { continue; }

      //Special case? Detect, apply, move on to next field
      if ( isSpecialCase(field, val, n) ) {
        continue;
      }

      //Is this another object? Should it even be explored?
      if (val instanceof ObjectReference && !exploreObject((ObjectReference) val)) {
        continue;
      }

      //OK finally, add it...
      n.addChild(getNode(val));
    }

    return n;
  }

  private boolean isSpecialCase(Field field, Value val, MutableNode n) {
    if (field.name().equals("memeographcolor") && field.typeName().equals("int")) {
        ((ObjectNode)n.gnt).color = new Color(((IntegerValue)val).value());
        return true;
    }else if (field.name().equals("memeographname") && field.typeName().equals("java.lang.String")){
        ((ObjectNode)n.gnt).name = ((StringReference)val).value();
        return true;
    }
    return false;
  }

  /**
   * False means don't explore it.
   * @param val
   * @return
   */
  private Pattern[] regexFilters;
  private HashMap<ObjectReference, Boolean> cache = new HashMap<ObjectReference, Boolean>();
  private boolean exploreObject(ObjectReference obj) {
    if (regexFilters == null) {
       Config config = Config.getConfig();
       String[] filters = config.getStringArrayProperty(Config.FILTERS);

       //Convert to regex...
       regexFilters = new Pattern[filters.length];
       for(int i = 0; i < filters.length; i++){
           StringBuilder regex = new StringBuilder("^");
           regex.append(filters[i].replaceAll("\\*", ".*")).append("$");
           regexFilters[i] = Pattern.compile(regex.toString());
       }
    }

    if (cache.containsKey(obj)) {
      return cache.get(obj);
    }

    String name = obj.referenceType().name();
    for (Pattern pattern : regexFilters) {
      if (pattern.matcher(name).matches()) {
        cache.put(obj, Boolean.FALSE);
        return false;
      }
    }

    cache.put(obj, Boolean.TRUE);
    return true;
  }

  private MutableNode getPrimitiveReference(Value val) {
    MutableNode n = new MutableNode();

    if (val instanceof BooleanValue) {
        n.gnt = new BooleanNode((BooleanValue)val);
    }else if (val instanceof CharValue){
        n.gnt = new CharNode((CharValue)val);
    }else if (val instanceof ShortValue){
        n.gnt = new ShortNode((ShortValue)val);
    }else if (val instanceof LongValue){
        n.gnt = new LongNode((LongValue)val);
    }else if (val instanceof IntegerValue){
        n.gnt = new IntegerNode((IntegerValue)val);
    }else if (val instanceof FloatValue){
        n.gnt = new FloatNode((FloatValue)val);
    }else if (val instanceof DoubleValue){
        n.gnt = new DoubleNode((DoubleValue)val);
    }else if (val instanceof ByteValue){
        n.gnt = new ByteNode((ByteValue)val);
    }else{
        n.gnt = new UnknownNode(val);
    }

    return n;
  }

}
