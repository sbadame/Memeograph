package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.awt.Color;
import java.util.*;

public class GraphBuilder {

    private VirtualMachine vm;

    private HashMap<StackFrame, StackObject> stackMap = new HashMap<StackFrame, StackObject>();
    private HashMap<ObjectReference, HeapObject> heapMap = new HashMap<ObjectReference, HeapObject>();
    private HashMap<ThreadReference, ThreadHeader> stacks = new HashMap<ThreadReference, ThreadHeader>();
    private SuperHeader supernode = new SuperHeader("Memeographer!");

    public GraphBuilder(VirtualMachine vm)
    {
        this.vm = vm;
        MethodEntryRequest entry = vm.eventRequestManager().createMethodEntryRequest();
        entry.addClassExclusionFilter("java.*");
        entry.addClassExclusionFilter("javax.*");
        entry.addClassExclusionFilter("sun.*");
        entry.setSuspendPolicy(entry.SUSPEND_ALL);
        entry.enable();

        MethodExitRequest exit = vm.eventRequestManager().createMethodExitRequest();
        exit.addClassExclusionFilter("java.*");
        exit.addClassExclusionFilter("javax.*");
        exit.addClassExclusionFilter("sun.*");
        exit.setSuspendPolicy(entry.SUSPEND_ALL);
        exit.enable();

        ThreadStartRequest threadStart = vm.eventRequestManager().createThreadStartRequest();
        threadStart.setSuspendPolicy(threadStart.SUSPEND_ALL);
        threadStart.enable();

        ThreadDeathRequest threadDeath = vm.eventRequestManager().createThreadDeathRequest();
        threadDeath.setSuspendPolicy(threadDeath.SUSPEND_ALL);
        threadDeath.enable();
    }

    /**
    * The String representation of an Object. NOTE: This needs to be unique
    * for every object. Otherwise building the graph will probably go wrong.
    */
    protected String ObjectReference2String(ObjectReference or){
            return or.referenceType().name() + "<" + or.uniqueID() + ">";
    }

    /**
    * The String representation of a Stack Frame. NOTE: This needs to be unique
    * for every object. Otherwise building the graph will probably go wrong.
    */
    protected String StackFrame2String(int depth, ThreadReference t) throws IncompatibleThreadStateException{
        if (t == null) {
            throw new NullPointerException("Thread Reference cannot be null.");
        }
        int count = t.frameCount() - depth - 1;
        try {
            return "Thread(" + t.name() + ") StackFrame(" + count + ") " + t.frame(depth).location().method().name();
        } catch (IncompatibleThreadStateException itse) {
            return "Thread(" + t.name() + ") StackFrame(" + count + ")";
        }
    }

    private StackObject getStackFrame(StackFrame f, int depth) throws IncompatibleThreadStateException{
        if (!stackMap.containsKey(f)){
                stackMap.put(f, new StackObject(StackFrame2String(depth, f.thread())));
        }
        return stackMap.get(f);
    }

    
    private StackObject exploreStackFrame(StackFrame frame, int depth) throws IncompatibleThreadStateException{
            StackObject tree = getStackFrame(frame, depth);
            ObjectReference thisor = frame.thisObject();
            if (thisor != null) {
                tree.addZChild(exploreObject(thisor));
            }

            try {
                    List<LocalVariable> locals = frame.visibleVariables();
                    LocalVariable[] localvars = locals.toArray(new LocalVariable[] {});
                    Arrays.sort(localvars);
                    for (LocalVariable var : localvars) {
                            Value val = frame.getValue(var);
                            if (val != null && val.type() != null && val.type() instanceof ClassType)
                                    tree.addZChild(exploreObject((ObjectReference)val));
                    }
            } catch (AbsentInformationException ex) {
                return tree;
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

    private HeapObject exploreObject(ObjectReference or){
            if (heapMap.containsKey(or)){
                DiGraph d = heapMap.get(or);
            }

            HeapObject tree = new HeapObject(ObjectReference2String(or));

            heapMap.put(or, tree); //Do this right off the bat to prevent infinite loop
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
                                 tree.addZChild(exploreObject(child));
                            } else if (((ClassType)or.type()).subclasses().contains(val.type())) {
                                 tree.addZChild(exploreObject(child));
                            } else if (((ClassType)val.type()).subclasses().contains(or.type())) {
                                 tree.addZChild(exploreObject(child));
                            } else {
                                 tree.addDataChild(exploreObject(child));
                            }
                        } else if (val.type() instanceof ArrayType){
                            System.err.println("Todo: Figure out what to do with arrays");
                        }else if (val.type() instanceof IntegerType){
                            IntegerValue iv = (IntegerValue)val;
                            tree.addDataChild(new HeapObject("int: " + new Integer(iv.intValue())));
                        }else if (val.type() instanceof BooleanValue){
                            BooleanValue iv = (BooleanValue)val;
                            tree.addDataChild(new HeapObject("bool: " + new Boolean(iv.booleanValue())));
                        }else if (val.type() instanceof ByteValue){
                            ByteValue iv = (ByteValue)val;
                            tree.addDataChild(new HeapObject("byte: " + new Byte(iv.byteValue())));
                        }else if (val.type() instanceof CharValue){
                            CharValue iv = (CharValue)val;
                            tree.addDataChild(new HeapObject("char: " + new Character(iv.charValue())));
                        }else if (val.type() instanceof DoubleValue){
                            DoubleValue iv = (DoubleValue)val;
                            tree.addDataChild(new HeapObject("double: " + new Double(iv.doubleValue())));
                        }else if (val.type() instanceof FloatValue){
                            FloatValue iv = (FloatValue)val;
                            tree.addDataChild(new HeapObject("float: " + new Float(iv.floatValue())));
                        }else if (val.type() instanceof LongValue){
                            LongValue iv = (LongValue)val;
                            tree.addDataChild(new HeapObject("long: " + new Long(iv.longValue())));
                        }else if (val.type() instanceof ShortValue){
                            ShortValue iv = (ShortValue)val;
                            tree.addDataChild(new HeapObject("short: " + new Short(iv.shortValue())));
                        }else{
                            System.err.println("Unknown data: " + val);
                        }
                    }
                }
            }
            return tree;
    }

    public SuperHeader getSuperNode(){
        return supernode;
    }

    private void addModifactionWatchpoint(Field field) {
        //ModificationWatchpointRequest r = vm.eventRequestManager().createModificationWatchpointRequest(field);
        //r.enable();
    }

  public void step(){
      //VM should already be suspended
      vm.resume();
      EventQueue eventQueue = vm.eventQueue();
      try {
          EventIterator eventIterator = eventQueue.remove().eventIterator();

          while (eventIterator.hasNext()) {
              Event event = eventIterator.nextEvent();
              if (event instanceof WatchpointEvent) {
                  WatchpointEvent we = (WatchpointEvent) event;
              } else if (event instanceof StepEvent) {
                  StepEvent se = (StepEvent) event;
                  System.out.println(se.location());
              } else if (event instanceof MethodEntryEvent) {
                  System.out.println("Down");
                  MethodEntryEvent mee = (MethodEntryEvent) event;
                  ThreadHeader thread = stacks.get(mee.thread());
                  if (thread == null) {
                      System.err.println("Method entry in unknown thread: " + mee.thread().name());
                  } else {
                      if (!mee.thread().isSuspended()) {
                          System.err.println("Thread: " + mee.thread() + " is not suspended");
                      }else{
                          try {
                              /*So we have a problem here:
                               frame(0) refers to the most current frame
                               The first element in the digraph represents
                               */
                              int frameCount = 0;
                              StackObject bottomframe = null;
                              if (thread.hasFrame()) {
                                  bottomframe = thread.getFrame();
                              }

                              while(bottomframe != null && bottomframe.hasNextFrame() ){
                                  StackObject bottomer = bottomframe.nextFrame();
                                  if (bottomframe == bottomer) {
                                    throw new RuntimeException("Cycle in stack");
                                  }
                                  bottomframe = bottomer;
                                  frameCount++;
                              }
                              int diff = mee.thread().frameCount() - frameCount;
                              for(int i = diff-1; i >= 0; i--){
                                  StackObject bottomer = exploreStackFrame(mee.thread().frame(i), i);
                                  bottomer.setColor(Color.RED);
                                  if (bottomframe == null) {
                                      thread.setFrame(bottomer);
                                  }else{
                                      bottomframe.setNextFrame(bottomer);
                                  }
                                  bottomframe = bottomer;
                              }
                          } catch (IncompatibleThreadStateException ex) {
                              ex.printStackTrace();
                          }
                      }
                  }
              } else if (event instanceof MethodExitEvent) {
                  System.out.println("Up");
                  MethodExitEvent mee = (MethodExitEvent) event;
                  ThreadHeader thread = stacks.get(mee.thread());
                  if (mee.thread().isSuspended() ) {
                      if (thread == null) {
                          System.err.println("Method exit in unknown thread: " + mee.thread().name());
                      } else {
                            try {
                                int framecount = mee.thread().frameCount();
                                StackObject bottom = thread.getFrame();
                                while(framecount > 0){
                                    bottom = bottom.nextFrame();
                                    framecount--;
                                }
                                bottom.removeNextFrame();
                            } catch (IncompatibleThreadStateException ex) {
                                ex.printStackTrace();
                            }

                      }
                  }else{
                      System.err.println("Thread: " + mee.thread().name()+ " is not suspended.");
                  }
              } else if (event instanceof ThreadStartEvent) {
                  ThreadStartEvent tse = (ThreadStartEvent) event;
                  if (!stacks.containsKey(tse.thread())) {
                      System.out.println("Starting: " + tse.thread().name());
                      ThreadHeader threadheader = new ThreadHeader(tse.thread());
                      stacks.put(tse.thread(), threadheader);
                      supernode.addThread(threadheader);
                  }
              } else if (event instanceof ThreadDeathEvent) {
                  ThreadDeathEvent tde = (ThreadDeathEvent) event;
                  System.out.println("Thread \""+tde.thread().name() + "\" has died.");
                  stacks.remove(tde.thread());
              } else if (event instanceof VMStartEvent) {
                  VMStartEvent se = (VMStartEvent) event;
                  for (ThreadReference threadReference : vm.allThreads()) {
                      ThreadHeader threadHeader = new ThreadHeader(threadReference);
                      stacks.put(threadReference, threadHeader);
                      supernode.addYChild(threadHeader);
                  }
                  //mainthread = se.thread();
                  //stacks.put(mainthread, new DiGraph(mainthread.name()));
                  //step = vm.eventRequestManager().createStepRequest(mainthread, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
                  //step.enable();
              } else {
                  System.err.println("Got an unexpected event" + event);
              }
              //If we're still waiting for the step event, then that means
              // that we just hanlded some other event. All events cause the
              //vm to freeze. That means that we need to resume the VM

              if (eventIterator.hasNext()) {
                  vm.resume();
              }
          }
      } catch (InterruptedException ex) {
          ex.printStackTrace();
      }

      System.out.println("Stacks");
      for (ThreadHeader threadHeader : supernode.getThreads()) {
          System.out.println("\t" + threadHeader.name());
      }
  }

}


