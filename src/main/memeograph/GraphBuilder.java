package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.lang.model.type.ArrayType;

public class GraphBuilder {

    private VirtualMachine vm;
    private ThreadReference mainthread;

    private HashMap<String, DiGraph> treeMap = new HashMap<String, DiGraph>();
    private HashMap<ThreadReference, DiGraph> stacks = new HashMap<ThreadReference, DiGraph>();

    private StepRequest step;

    private boolean built = false;

    public GraphBuilder(VirtualMachine vm)
    {
        this.vm = vm;
    }

    public void addEventRequests()
    {
        MethodEntryRequest entry = vm.eventRequestManager().createMethodEntryRequest();
        entry.enable();

        MethodExitRequest exit = vm.eventRequestManager().createMethodExitRequest();
        exit.enable();

        ThreadStartRequest threadStart = vm.eventRequestManager().createThreadStartRequest();
        threadStart.enable();

        ThreadDeathRequest threadDeath = vm.eventRequestManager().createThreadDeathRequest();
        threadDeath.enable();

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
    private void buildStack(ThreadReference t) {
       try {
           int i = 0;
           for (StackFrame frame: t.frames()) {
               DiGraph tree = exploreStackFrame(frame, i);
               tree.setColor(Color.red);
               //Now where to put this tree...?
               if (i ==  t.frameCount() - 1){ //The top most frame
                   stacks.put(t, tree);
               }else{ 
                   //Just add to the previous Stack Frame DiGraph
                   //add as a software child to go down the y direction
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
                tree.addDataChild(exploreObject(thisor));
            }

            try {
                    List<LocalVariable> locals = frame.visibleVariables();
                    LocalVariable[] localvars = locals.toArray(new LocalVariable[] {});
                    Arrays.sort(localvars);
                    for (LocalVariable var : localvars) {
                            Value val = frame.getValue(var);
                            if (val != null && val.type() != null && val.type() instanceof ClassType)
                                    tree.addDataChild(exploreObject((ObjectReference)val));
                    }
            } catch (AbsentInformationException ex) {
                return tree;
                    //Seems to only be thrown when we see a frame with no variables
                    //that we can access, not sure if this something need be looked into
                    //System.err.println("AbsentInformaionException at " + StackFrame2String(depth));
                    //System.err.println(ex);
            }
            return tree;
    }

    protected boolean filterObject(ObjectReference o){
            if (o.referenceType().name().startsWith("java.")) return false;
            if (o.referenceType().name().startsWith("com.sun.")) return false;
            if (o.referenceType().name().startsWith("sun.")) return false;
            if (o.referenceType().name().startsWith("javax.")) return false;
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
                Field[] allFields = or.referenceType().allFields().toArray(new Field[] {});
                Arrays.sort(allFields);

                for (Field field : allFields) {
                    if (field.name().equals("memeographname") && field.typeName().equals("java.lang.String") ){
                        if (or.getValue(field) == null){ continue; }
                        String treetxt = or.getValue(field).toString();
                        treetxt = treetxt.substring(1, treetxt.length()-1);
                        tree.setData(treetxt);
                    }else if (field.name().equals("memeographcolor") && field.typeName().equals("java.awt.Color")){
                        ObjectReference colorref = (ObjectReference) or.getValue(field);
                        if (colorref == null) { continue; } //Got this Nullpointer some how...
                        Value color_value = colorref.getValue(colorref.referenceType().fieldByName("value"));
                        IntegerValue iv = (IntegerValue)color_value;
                        tree.setColor(new Color(iv.intValue()));
                    }else{
                        addModifactionWatchpoint(field);
                        Value val = or.getValue(field);
                        if(val == null || val.type() == null)continue;
                        if ( val != null && val.type() != null && val.type() instanceof ClassType ){
                             ObjectReference child = (ObjectReference) val;
                             if (or.referenceType().name().equals(child.referenceType().name())) {
                                 tree.addDataChild(exploreObject(child));
                            } else if (((ClassType)or.type()).subclasses().contains(val.type())) {
                                 tree.addDataChild(exploreObject(child));
                            } else if (((ClassType)val.type()).subclasses().contains(or.type())) {
                                 tree.addDataChild(exploreObject(child));
                            } else {
                                 tree.addSoftwareChild(exploreObject(child));
                            }
                        } else if (val.type() instanceof ArrayType){
                            System.err.println("Todo: Figure out what to do with arrays");
                        }else if (val.type() instanceof IntegerType){
                            IntegerValue iv = (IntegerValue)val;
                            tree.addSoftwareChild(new DiGraph("int: " + new Integer(iv.intValue())));
                        }else if (val.type() instanceof BooleanValue){
                            BooleanValue iv = (BooleanValue)val;
                            tree.addSoftwareChild(new DiGraph("bool: " + new Boolean(iv.booleanValue())));
                        }else if (val.type() instanceof ByteValue){
                            ByteValue iv = (ByteValue)val;
                            tree.addSoftwareChild(new DiGraph("byte: " + new Byte(iv.byteValue())));
                        }else if (val.type() instanceof CharValue){
                            CharValue iv = (CharValue)val;
                            tree.addSoftwareChild(new DiGraph("char: " + new Character(iv.charValue())));
                        }else if (val.type() instanceof DoubleValue){
                            DoubleValue iv = (DoubleValue)val;
                            tree.addSoftwareChild(new DiGraph("double: " + new Double(iv.doubleValue())));
                        }else if (val.type() instanceof FloatValue){
                            FloatValue iv = (FloatValue)val;
                            tree.addSoftwareChild(new DiGraph("float: " + new Float(iv.floatValue())));
                        }else if (val.type() instanceof LongValue){
                            LongValue iv = (LongValue)val;
                            tree.addSoftwareChild(new DiGraph("long: " + new Long(iv.longValue())));
                        }else if (val.type() instanceof ShortValue){
                            ShortValue iv = (ShortValue)val;
                            tree.addSoftwareChild(new DiGraph("short: " + new Short(iv.shortValue())));
                        }else{
                            System.err.println("Unknown data: " + val);
                        }
                    }
                }
            }
            return treeMap.get(txt);
    }

    /*public DiGraph getGraph(){
            return graph;
    }*/

    public Collection<DiGraph> getStacks(){
        return stacks.values();
    }

    public HashMap<String, DiGraph> getGraphMap(){
            return treeMap;
    }

    private void addModifactionWatchpoint(Field field) {
        ModificationWatchpointRequest r = vm.eventRequestManager().createModificationWatchpointRequest(field);
        r.enable();
    }

  public void step(){
      //VM should already be suspended
      built = false; //Tell that world that we're back to building...
      vm.resume();
      EventQueue eventQueue = vm.eventQueue();
      boolean waitingforstep = true;
      while(waitingforstep){
            try {
                EventIterator eventIterator = eventQueue.remove().eventIterator();
                
                while(eventIterator.hasNext()){
                    Event event = eventIterator.nextEvent();
                    if (event instanceof WatchpointEvent) {
                        WatchpointEvent we = (WatchpointEvent)event;
                    }else if (event instanceof StepEvent){
                        StepEvent se = (StepEvent)event;
                        System.out.println(se.location());
                        waitingforstep = false;
                    }else if (event instanceof MethodEntryEvent){
                        System.out.println("Down");
                        MethodEntryEvent mee = (MethodEntryEvent)event;
                        DiGraph topframe = stacks.get(mee.thread());
                        if (topframe == null) {
                            System.err.println("Method entry in unknown thread: " + mee.thread().name());
                        }else{
                            DiGraph frame = topframe;
                            while(frame.getSoftwareChildren().size() >  0){
                                frame = frame.getSoftwareChildren().firstElement();
                            }
                            try {
                                if (mee.thread().isSuspended()) {
                                    DiGraph newframe = exploreStackFrame(mee.thread().frame(0), 0);
                                    if (!treeMap.containsValue(newframe)) {
                                        frame.addSoftwareChild(newframe);
                                    }
                                }else{
                                    System.err.println("Thread: \"" + mee.thread().name() + "\" is not suspended");
                                }
                            } catch (IncompatibleThreadStateException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }else if (event instanceof MethodExitEvent){
                        System.out.println("Up");
                        MethodExitEvent mee = (MethodExitEvent)event;
                        DiGraph topframe = stacks.get(mee.thread());
                        if (topframe == null) {
                            System.err.println("Method exit in unknown thread: " + mee.thread().name());
                        }else{
                            DiGraph frame = topframe;
                            while(frame.getSoftwareChildren().size() > 0){
                                frame = frame.getSoftwareChildren().firstElement();
                            }
                            if (frame != topframe) {
                                DiGraph parent = frame.getSoftwareParents().get(0);
                                parent.removeSoftwareChildren();
                            }
                        }
                    }else if (event instanceof ThreadStartEvent){
                        ThreadStartEvent tse = (ThreadStartEvent)event;
                        System.out.println(tse.thread().name());
                        stacks.put(tse.thread(), new DiGraph(tse.thread().name()));
                    }else if (event instanceof ThreadDeathEvent){
                        ThreadDeathEvent tde = (ThreadDeathEvent)event;
                        System.out.println(tde.thread().name());
                        stacks.remove(tde.thread());
                    }else if (event instanceof VMStartEvent){
                        VMStartEvent se = (VMStartEvent)event;
                        for (ThreadReference threadReference : vm.allThreads()) {
                            stacks.put(threadReference, new DiGraph(threadReference.name()));
                        }
                        mainthread = se.thread();
                        stacks.put(mainthread, new DiGraph(mainthread.name()));
                        step = vm.eventRequestManager().createStepRequest(mainthread, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
                        step.enable();
                    }else{
                        System.err.println("Got an unexpected event" + event);
                    }
                    //If we're still waiting for the step event, then that means
                    // that we just hanlded some other event. All events cause the
                    //vm to freeze. That means that we need to resume the VM
                    if (waitingforstep) {
                        vm.resume();
                    }
                }
            } catch (InterruptedException ex) {
               ex.printStackTrace();
            }
      }
      built = true;
  }

    /**
     * @return the built
     */
    public boolean isBuilt() {
        return built;
    }

}
