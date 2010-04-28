package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.Value;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphBuilder {

    private VirtualMachine vm;

    private HashMap<StackFrame, StackObject> stackMap = new HashMap<StackFrame, StackObject>();
    private HashMap<ThreadReference, ThreadHeader> stacks = new HashMap<ThreadReference, ThreadHeader>();

    //Interrogate code
    private SuperHeader supernode = new SuperHeader("Memeographer!");
    private HeapObjectFactory hof = new HeapObjectFactory();

    public GraphBuilder(VirtualMachine vm)
    {
        this.vm = vm;
        hof.addFilter("java");
        hof.addFilter("sun");
        try {
            EventSet eset = vm.eventQueue().remove();
            EventIterator eventIterator = eset.eventIterator();
            while(eventIterator.hasNext()){
                Event event = eventIterator.nextEvent();
                if (event instanceof VMStartEvent) {
                    VMStartEvent vmse = (VMStartEvent) event;
                    StepRequest createStepRequest = vm.eventRequestManager().createStepRequest(vmse.thread(),
                                                                                               StepRequest.STEP_MIN,
                                                                                               StepRequest.STEP_INTO);
                    createStepRequest.enable();
                }
            }
            eset.resume();
        } catch (InterruptedException ex) {
        }
    }

    /*
     * Clear the system of any memory of a graph, then interrogate the
     * system for its current state
     */
    public void interrogate(){
        reset();

        //Go through all of the threads
        for (ThreadReference thread : vm.allThreads()) {
            try {
                ThreadHeader header = new ThreadHeader(thread);
                supernode.addThread(header);
                StackObject prev = null;
                for (int depth = thread.frameCount() - 1; depth > 0; depth--) {
                    StackObject so = exploreStackFrame(thread.frame(depth), depth);
                    if (prev != null) {
                        prev.setNextFrame(so);
                    }
                    prev = so;
                }
            } catch (IncompatibleThreadStateException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void reset(){
        supernode.removeChildren();
        hof.reset();
        stackMap.clear();
        stacks.clear();
    }

    private StackObject exploreStackFrame(StackFrame frame, int depth) throws IncompatibleThreadStateException{
            StackObject tree = getStackFrame(frame, depth);
            ObjectReference thisor = frame.thisObject();
            if (thisor != null) {
                tree.addHeapObject(hof.getHeapObject(thisor));
            }
            try {
                List<LocalVariable> locals = frame.visibleVariables();
                LocalVariable[] localvars = locals.toArray(new LocalVariable[] {});
                Arrays.sort(localvars);
                for (LocalVariable var : localvars) {
                        Value val = frame.getValue(var);
                        if (val != null && val.type() != null)
                                tree.addHeapObject(hof.getHeapObject(val));
                }
            } catch (AbsentInformationException ex) {
                return tree;
            }
            return tree;
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

    public SuperHeader getSuperNode(){
        return supernode;
    }

    public void step(){
        try {
            //Read in this set of events
            EventSet eventSet = vm.eventQueue().remove();
            interrogate();
            //Resume the VM
            eventSet.resume();
        } catch (InterruptedException ex) {
            System.err.println("Couldn't retreive the eventset in the queue");
            ex.printStackTrace();
        }
    }

    private void handleMethodEntry(MethodEntryEvent mee) {
      ThreadHeader thread = stacks.get(mee.thread());
      try {
          System.out.print("MethodEntry: ");
          for (int i = mee.thread().frameCount()-1; i >= 0; i--) {
              System.out.print(mee.thread().frame(i).location() + "-> ");
          }
          System.out.println("[]");
      } catch (IncompatibleThreadStateException ex) {
          System.err.println("BAD TRHEAD STATE??!?");
          ex.printStackTrace();
      }
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
                      bottomer.setColor(java.awt.Color.RED);
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
    }

    private void handleMethodExit(MethodExitEvent mee) {
      ThreadHeader thread = stacks.get(mee.thread());
      try {
          System.out.print("MethodExit: ");
          for (int i = mee.thread().frameCount()-1; i >= 0; i--) {
              System.out.print(mee.thread().frame(i).location() + "-> ");
          }
          System.out.println("[]");
      } catch (IncompatibleThreadStateException ex) {
          System.err.println("BAD TRHEAD STATE??!?");
          ex.printStackTrace();
      }
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
    }

    private void handleThreadStart(ThreadStartEvent tse) {
      if (!stacks.containsKey(tse.thread())) {
          System.out.println("Starting: " + tse.thread().name());
          ThreadHeader threadheader = new ThreadHeader(tse.thread());
          stacks.put(tse.thread(), threadheader);
          supernode.addThread(threadheader);
      }
    }

    private void handleThreadDeath(ThreadDeathEvent tde){
      System.out.println("Thread \""+tde.thread().name() + "\" has died.");
      stacks.remove(tde.thread());
    }

    private void handleVMStartEvent(VMStartEvent se){
      for (ThreadReference threadReference : vm.allThreads()) {
          ThreadHeader threadHeader = new ThreadHeader(threadReference);
          stacks.put(threadReference, threadHeader);
          supernode.addYChild(threadHeader);
      }
    }

    private void handleModificationWatchpointEvent(ModificationWatchpointEvent mwe) {
        System.out.println(mwe.field() + ": " + mwe.valueCurrent() + " -> " + mwe.valueToBe());
    }
}


