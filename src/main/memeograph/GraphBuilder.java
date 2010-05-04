package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.Value;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import com.sun.tools.jdi.EventSetImpl;
import java.util.*;

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

       //We need this catch the loading of classes
       ClassPrepareRequest classEvent = vm.eventRequestManager().createClassPrepareRequest();
       classEvent.addClassExclusionFilter("java.*");
       classEvent.addClassExclusionFilter("javax.*");
       classEvent.addClassExclusionFilter("sun.*");
       classEvent.enable();
    }

    /*
     * Clear the system of any memory of a graph, then interrogate the
     * system for its current state
     */
    public void interrogate(){
        hof = new HeapObjectFactory(new String[]{"java", "sun"});
        supernode = new SuperHeader("Memeographer!");
        stackMap = new HashMap<StackFrame, StackObject>();
        stacks = new HashMap<ThreadReference, ThreadHeader>();

        //Go through all of the threads
        for (ThreadReference thread : vm.allThreads()) {
            if (thread.threadGroup().name().equals("system")) {
                continue;
            }
            try {
                ThreadHeader header = new ThreadHeader(thread);
                supernode.addThread(header);
                StackObject prev = null;

                for (int depth = thread.frameCount() - 1; depth > 0; depth--) {
                    StackObject so = exploreStackFrame(thread.frame(depth), depth);
                    //System.out.println("StackObject: " + so);
                    if (prev != null) {
                        prev.setNextFrame(so);
                    } else {
                        header.setFrame(so);
                    }
                    prev = so;
                }
            } catch (IncompatibleThreadStateException ex) {
                ex.printStackTrace();
            }
        }

        //System.out.println("Super Node! is: " + supernode);
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
            boolean looking_for_pause = true;
            while(looking_for_pause){
                //Read in this set of events
                EventSet eventSet = vm.eventQueue().remove();
                EventIterator eventIterator = eventSet.eventIterator();
                while(eventIterator.hasNext()){
                    Event event = eventIterator.nextEvent();
                    if (event instanceof VMStartEvent){
                        System.out.println("VM started");
                    }else if (event instanceof ModificationWatchpointEvent) {
                        looking_for_pause = false;
                    }else if (event instanceof ClassPrepareEvent){
                        ClassPrepareEvent cpe = (ClassPrepareEvent) event;
                        System.out.println(cpe.referenceType().name());
                        for (Field field : cpe.referenceType().allFields()) {
                            if (field.name().equals("memeopoint")) {
                                ModificationWatchpointRequest mwr = vm.eventRequestManager().createModificationWatchpointRequest(field);
                                mwr.enable();
                            }
                        }
                    }else{
                        System.err.println("Caught the wrong watchpoint" + event.getClass().getName());
                    }
                }
                eventSet.resume();
            }
            interrogate();
            //Resume the VM
        } catch (InterruptedException ex) {
            System.err.println("Couldn't retreive the eventset in the queue");
            ex.printStackTrace();
        }
    }
}


