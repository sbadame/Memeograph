package memeograph;

import com.sun.jdi.*;
import java.util.*;

public class HeapObjectFactory {

    private HashMap<Value, HeapObject> heapMap = new HashMap<Value, HeapObject>();
    private ArrayList<String> filters = new ArrayList<String>();

    public HeapObjectFactory(){

    }

    public HeapObject getHeapObject(Value val){
        if (heapMap.containsKey(val)) {
            return heapMap.get(val);
        }
        HeapObject heapObject = new HeapObject();
        heapMap.put(val, heapObject);
        if (val instanceof ArrayReference){
            //We have to check ArrayReference first since ArrayReference extends ObjectReference
            ArrayReference ar = (ArrayReference)val;
            String name = ar.type().name();
            name = name.substring(0, name.length()-1) + ar.length() + "]";
            heapObject.setName(name);
            for (Value value : ar.getValues()) {
                if (value == null) {
                    //TODO: Why are there null values that pop up?
                    continue;
                }
                heapObject.addSoftwareChild(getHeapObject(value));
            }
        }else if (val instanceof ObjectReference) {
            ObjectReference or = (ObjectReference) val;
            ClassType ct = (ClassType) val.type();
            heapObject.setName(or.referenceType().name() + "<" + or.uniqueID() + ">");
            List<Field> allFields = ct.allFields();
            Field[] fieldArray = allFields.toArray(new Field[]{});
            Arrays.sort(fieldArray);
            for (Field field : fieldArray) {
                System.out.println(field);
                Value fieldvalue = or.getValue(field);
                //Check if this field is a special case
                SpecialField specialCase = SpecialField.getSpecialField(field, val);
                if (specialCase != null) {
                    specialCase.apply(heapObject, field, fieldvalue);
                    System.out.println("\t" + "Field is a special case");
                    continue;
                }else{
                    System.out.println("\t" + "Field is not a special case");
                }
                if (fieldvalue == null) { System.out.println("\tField is null"); continue; }
                if (fieldvalue == val) {
                    System.out.println("\tField is referring to self");
                    continue;
                }
                System.out.println("\t" + "Field is not null");
                if (fieldvalue instanceof ObjectReference){
                    System.out.println("\t" + "Field is a data child");
                    ObjectReference objectref = (ObjectReference)fieldvalue;
                    if (filterObject(objectref)) {continue;}
                    if (isDataChild(or, objectref)) {
                        HeapObject newho = getHeapObject(fieldvalue);
                        heapObject.addDataChild(newho);
                    }else{
                        heapObject.addSoftwareChild(getHeapObject(fieldvalue));
                    }
                }else{
                    System.out.println("\t" + "Field is not a data child");
                    heapObject.addSoftwareChild(getHeapObject(fieldvalue));
                }
            }

        }else if (val.type() instanceof IntegerType){
            IntegerValue iv = (IntegerValue)val;
            heapObject.setName("int: " + iv.intValue());
        }else if (val.type() instanceof BooleanValue){
            BooleanValue iv = (BooleanValue)val;
            heapObject.setName("bool: " + iv.booleanValue());
        }else if (val.type() instanceof ByteValue){
            ByteValue iv = (ByteValue)val;
            heapObject.setName("byte: " + iv.byteValue());
        }else if (val.type() instanceof CharValue){
            CharValue iv = (CharValue)val;
            heapObject.setName("char: " + iv.charValue());
        }else if (val.type() instanceof DoubleValue){
            DoubleValue iv = (DoubleValue)val;
            heapObject.setName("double: " + iv.doubleValue());
        }else if (val.type() instanceof FloatValue){
            FloatValue iv = (FloatValue)val;
            heapObject.setName("float: " + iv.floatValue());
        }else if (val.type() instanceof LongValue){
            LongValue iv = (LongValue)val;
            heapObject.setName("long: " + iv.longValue());
        }else if (val.type() instanceof ShortValue){
            ShortValue iv = (ShortValue)val;
            heapObject.setName("short: " + iv.shortValue());
        }else{
            System.err.println("Unknown data: " + val);
        }
        return heapObject;

    }

   public void addFilter(String filter){
       filters.add(filter);
   }

   private boolean filterObject(ObjectReference o){
       String name = o.referenceType().name();
       for (String filter : filters) {
           if (name.startsWith(filter)) {
               return true;
           }
       }
       return false;
    }

   private static boolean isDataChild(ObjectReference a, ObjectReference b){
       if (a.referenceType() instanceof ArrayType || b.referenceType() instanceof ArrayType) {
           return false;
       }
       ClassType atype = (ClassType) a.referenceType();
       ClassType btype = (ClassType) b.referenceType();
       System.out.println("Comparing types:");
       System.out.println("\t" + atype.name());
       System.out.println("\t" + btype.name());
       boolean isDataChild = atype.name().equals(btype.name()) || atype.subclasses().contains(btype) || btype.subclasses().contains(atype);
       System.out.println("\t" + isDataChild);
       return isDataChild;
   }

    void reset() {
        heapMap.clear();
    }

}
