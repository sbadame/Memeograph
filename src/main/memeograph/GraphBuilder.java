package memeograph;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;

public class GraphBuilder {

    private VirtualMachine vm;
    private DiGraph graph =  new DiGraph("Memeograph!");
    private HashMap<String, DiGraph> treeMap = new HashMap<String, DiGraph>();

    public GraphBuilder(VirtualMachine vm)
    {
        this.vm = vm;
    }

    public void buildGraph()
    {
        vm.suspend();

        //First we go through all of the loaded classes
        //Do we really need to do this?
        for(ReferenceType c : vm.allClasses()){
                searchClass(c);
        }

        //Now we go through all of the threads
        for (ThreadReference t : vm.allThreads()) {
                getTree(t);
        }

        vm.resume();
    }

    private void searchClass(ReferenceType t){
        for (ObjectReference o : t.instances(0)) {
                exploreObject(o);
        }
    }

    /**
    * The String representation of an Object. NOTE: This needs to be unique
    * for every object. Otherwise building the graph will probably go wrong.
    */
    protected String getText(ObjectReference or){
            return or.referenceType().name() + "<" + or.uniqueID() + ">";
    }

    /**
    * The String representation of a Stack Frame. NOTE: This needs to be unique
    * for every object. Otherwise building the graph will probably go wrong.
    */
    protected String StackFrame2String(int depth, ThreadReference t) throws IncompatibleThreadStateException{
        int count = t.frameCount() - depth - 1;
        try {
            return "Thread(" + t.name() + ") StackFrame(" + count + ") " + t.frame(depth).location().method().name();
        } catch (IncompatibleThreadStateException itse) {
            return "Thread(" + t.name() + ") StackFrame(" + count + ")";
        }
    }


    /**
     * Traverses the frames of this thread until it reaches local 
     * variables
     */
    private void getTree(ThreadReference t) {
       //t.frame[0] == current frame
       //t.frame[t.frameCount() - 1] == top most frame
       try {
           int i = 0;
           for (StackFrame frame: t.frames()) {
               DiGraph tree = exploreStackFrame(frame, i);
 
               //Now where to put this tree...?
               if (i ==  t.frameCount() - 1){ //The top most frame
                   graph.addSoftwareChild(tree);
               }else{ //Just add to the previous Stack Frame DiGraph
                   getStackFrame(i+1, t).addSoftwareChild(tree);
               }
               i++;
           }
       } catch (IncompatibleThreadStateException itse) {
           System.err.println("Why in the world do we have an IncompatibleThreadStateException?");
           itse.printStackTrace();
       }
    }

    private DiGraph getStackFrame(int depth, ThreadReference t) throws IncompatibleThreadStateException{
        String key = StackFrame2String(depth, t);
        if (!treeMap.containsKey(key)){
                treeMap.put(key, new DiGraph(key));
        }
        return treeMap.get(key);
    }

    
    private DiGraph exploreStackFrame(StackFrame frame, int depth) throws IncompatibleThreadStateException{
            DiGraph tree = getStackFrame(depth, frame.thread());
            ObjectReference thisor = frame.thisObject();
            if (thisor != null) {
                tree.addSoftwareChild(exploreObject(thisor));
            }

            try {
                    for (Value val : frame.getValues(frame.visibleVariables()).values()) {
                            if (val != null && val.type() != null && val.type() instanceof ClassType)
                                    tree.addSoftwareChild(exploreObject((ObjectReference)val));
                    }
            } catch (AbsentInformationException ex) {
                    //Seems to only be thrown when we see a frame with no variables
                    //that we can access, not sure if this something need be looked into
                    //System.err.println("AbsentInformaionException at " + StackFrame2String(depth));
                    //System.err.println(ex);
            }
            return tree;
    }

    protected boolean filterObject(ObjectReference o){
            if (o.referenceType().name().startsWith("java.")) return false;
            if (o.referenceType().name().startsWith("sun.")) return false;
            return true;
    }

    private DiGraph exploreObject(ObjectReference or){
            String txt = getText(or);
            if (treeMap.containsKey(txt)){
                    return treeMap.get(txt);
            }

            DiGraph tree = new DiGraph();
            tree.setData(txt);
            treeMap.put(txt, tree); //Do this right off the bat to prevent infinite loop
                                                            //With cycling graphs
            if ( filterObject(or) ){
                List<Field> allFields = or.referenceType().allFields();
                for (Field field : allFields) {
                    if (field.name().equals("memeographname") && field.typeName().equals("java.lang.String") ){
                        String treetxt = or.getValue(field).toString();
                        treetxt = treetxt.substring(1, treetxt.length()-1);
                        tree.setData(treetxt);
                    }else if (field.name().equals("memeographcolor") && field.typeName().equals("java.awt.Color")){
                        ObjectReference colorref = (ObjectReference) or.getValue(field);
                        Value color_value = colorref.getValue(colorref.referenceType().fieldByName("value"));
                        IntegerValue iv = (IntegerValue)color_value;
                        tree.setColor(new Color(iv.intValue()));
                    }else{
                        Value val = or.getValue(field);
                        if ( val != null && val.type() != null && val.type() instanceof ClassType ){
                             ObjectReference child = (ObjectReference) val;
                             if (or.referenceType().name().equals(child.referenceType().name())) {
                                 tree.addDataChild(exploreObject(child));
                            }else{
                                 tree.addSoftwareChild(exploreObject(child));
                            }
                        }
                    }
                }
            }
            return treeMap.get(txt);
    }

    public DiGraph getGraph(){
            return graph;
    }

    public HashMap<String, DiGraph> getGraphMap(){
            return treeMap;
    }

}
