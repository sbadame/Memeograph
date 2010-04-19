package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.Value;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.awt.Color;
import java.util.*;

public class GraphBuilder {

    private VirtualMachine vm;

    private HashMap<StackFrame, StackObject> stackMap = new HashMap<StackFrame, StackObject>();
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
                tree.addHeapObject(HeapObject.getHeapObject(thisor));
            }
            try {
                List<LocalVariable> locals = frame.visibleVariables();
                LocalVariable[] localvars = locals.toArray(new LocalVariable[] {});
                Arrays.sort(localvars);
                for (LocalVariable var : localvars) {
                        Value val = frame.getValue(var);
                        if (val != null && val.type() != null)
                                tree.addHeapObject(HeapObject.getHeapObject(val));
                }
            } catch (AbsentInformationException ex) {
                return tree;
            }
            return tree;
    }

    
    
    public SuperHeader getSuperNode(){
        return supernode;
    }

  public void step(){
      //VM should already be suspended
      vm.resume();
      EventQueue eventQueue = vm.eventQueue();
      System.out.println("STEPPING");
      try {
          EventIterator eventIterator = eventQueue.remove().eventIterator();

          while (eventIterator.hasNext()) {
              Event event = eventIterator.nextEvent();
              if (event instanceof ModificationWatchpointEvent) {
                  ModificationWatchpointEvent mwe = (ModificationWatchpointEvent)event;
                  System.out.println(mwe.field() + ": "+ mwe.valueCurrent() + " -> " + mwe.valueToBe());
                  continue;
              } else if (event instanceof StepEvent) {
                  StepEvent se = (StepEvent) event;
              } else if (event instanceof MethodEntryEvent) {
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

  }

}


