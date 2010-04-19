package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.Value;
import com.sun.jdi.request.ModificationWatchpointRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HeapObject extends DiGraph{

    //Keeps a pool of heap objects
    public static HashMap<Value, HeapObject> heapMap = new HashMap<Value, HeapObject>();

   /**
    * The String representation of a fieldvalue
    */
    public static HeapObject getHeapObject(Value val){
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
                addWatchpoint(field);
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
                System.out.println("\t" + "Field is not null");
                if (fieldvalue instanceof ObjectReference){
                    System.out.println("\t" + "Field is a data child");
                    ObjectReference objectref = (ObjectReference)fieldvalue;
                    if (filterObject(objectref)) {continue;}
                    if (isDataChild(or, objectref)) {
                        heapObject.addDataChild(getHeapObject(fieldvalue));
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

   private static boolean filterObject(ObjectReference o){
        if (o.referenceType().name().startsWith("java.")) return true;
        if (o.referenceType().name().startsWith("com.sun.")) return true;
        if (o.referenceType().name().startsWith("sun.")) return true;
        if (o.referenceType().name().startsWith("javax.")) return true;
        return false;
    }

   private static boolean isDataChild(ObjectReference a, ObjectReference b){
       ClassType atype = (ClassType) a.referenceType();
       ClassType btype = (ClassType) b.referenceType();
       System.out.println("Comparing types:");
       System.out.println("\t" + atype.name());
       System.out.println("\t" + btype.name());
       boolean isDataChild = atype.name().equals(btype.name()) || atype.subclasses().contains(btype) || btype.subclasses().contains(atype);
       System.out.println("\t" + isDataChild);
       return isDataChild;
   }

    private static void addWatchpoint(Field field){
        ModificationWatchpointRequest wp = field.virtualMachine().eventRequestManager().createModificationWatchpointRequest(field);
        wp.setSuspendPolicy(wp.SUSPEND_ALL);
        wp.enable();
    }

    public static void remove(Value val){
        if (!heapMap.containsKey(val)) { return; }
    }





    private HeapObject(){
        super();
    }

    public void setName(String name){
        setData(name);
    }

    public String getName(){
        return getData();
    }

    public void addDataChild(HeapObject child){
        addZChild(child);
    }

    public Iterable<HeapObject> getDataChildren(){
        return getZIterator();
    }

    public void addSoftwareChild(HeapObject ho){
        addYChild(ho);
    }

    public Iterable<HeapObject> getSoftwareChildren(){
        return getYIterator();
    }
  }
