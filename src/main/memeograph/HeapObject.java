package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.request.ModificationWatchpointRequest;
import java.util.HashMap;

public class HeapObject extends DiGraph{

    //Keeps a pool of heap objects
    private static HashMap<Value, HeapObject> heapMap = new HashMap<Value, HeapObject>();

   /**
    * The String representation of a value
    */
    public static HeapObject getHeapObject(Value val){
        if (heapMap.containsKey(val)) {
            return heapMap.get(val);
        }
        HeapObject heapObject = new HeapObject();
        heapMap.put(val, heapObject);
        if (val instanceof ObjectReference) {
            ObjectReference or = (ObjectReference) val;
            ClassType ct = (ClassType) val.type();
            heapObject.setName(or.referenceType().name() + "<" + or.uniqueID() + ">");

            for (Field field : ct.allFields()) {
                addWatchpoint(field);
                System.out.println(field);
                Value value = or.getValue(field);
                if (value == null) { continue; }
                System.out.println("\t" + "Field is not null");
                //Check if this field is a special case
                SpecialFieldCase specialCase = SpecialFieldCase.getSpecialCase(field, val);
                if (specialCase != null) {
                    specialCase.apply(heapObject, field, val);
                    continue;
                }
                System.out.println("\t" + "Field is not a special case");
                if (value instanceof ObjectReference){
                    System.out.println("\t" + "Field is a data child");
                    ObjectReference objectref = (ObjectReference)value;
                    if (filterObject(objectref)) {continue;}
                    if (isDataChild(or, objectref)) {
                        heapObject.addDataChild(getHeapObject(value));
                    }else{
                        heapObject.addSoftwareChild(getHeapObject(value));
                    }
                }else{
                    System.out.println("\t" + "Field is not a data child");
                    heapObject.addSoftwareChild(getHeapObject(value));
                }
            }

        }else if (val instanceof ArrayReference){
            heapObject.setName("Array");
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
