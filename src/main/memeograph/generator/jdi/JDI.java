package memeograph.generator.jdi;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import memeograph.generator.jdi.nodes.*;

import java.util.*;
import java.io.IOException;

import memeograph.Generator;
import memeograph.Config;
import memeograph.graph.Graph;
import memeograph.graph.MutableNode;
import memeograph.graph.Node;

/**
* This will generateGraph a VM for an object graph whenever a trigger method is
* called in the SuT.
*
* Has the slightly strange side effect that if "jdbgrapher.savefile" is set it
* will serialize the graph and save it to the filename pointed to by the property.
* that graph can then be loaded using the GraphFileLoader which is much faster
* then re-running the program and building the graph from scratch in most
* cases.
*/
public class JDI implements Generator {
    private final HashMap<Long, MutableNode> objectCache = new HashMap<Long, MutableNode>();
    private final ValueNodeCreator valueCache = new ValueNodeCreator();

    private VirtualMachine vm;
    private ThreadReference mainThread;
    private final String target;
    private final String target_args;
    private EventIterator eventIterator = null;
    private HashMap<EventRequest, EventAction> actions = new HashMap<EventRequest, EventAction>();
    private boolean hasDied = false;
    private final String triggermethodname;
    private final String triggerclassname;

    public JDI(Config config){
        String target_options = config.getProperty(Config.TARGET_OPTIONS, "");
        target = target_options.substring(target_options.lastIndexOf(' '));
        target_args = target_options.substring(0, target_options.lastIndexOf(' '));
        
        String t = Config.getConfig().getProperty(Config.TRIGGER);
        triggermethodname = t.substring(t.lastIndexOf('.')+1);
        triggerclassname = t.substring(0, t.lastIndexOf('.'));
    }

    @Override
    public void start(){
        boolean keepgoing = true;
        for (LaunchingConnector connector : Bootstrap.virtualMachineManager().launchingConnectors()) {
                if (!keepgoing || connector.transport().name().equals("dt_socket") == false) {
                    continue;
                }
                Map<String, Argument> launchargs = connector.defaultArguments();
                launchargs.get("options").setValue(target_args);
                launchargs.get("main").setValue(target);
                try {
                    vm = connector.launch(launchargs);
                    new ProcessDirector(vm.process()).start();
                    Config.getConfig().put(Config.TARGET_MAIN, target.trim());
                    Config.getConfig().put(Config.TARGET_ARGS, target_args.trim());
                    keepgoing = false;
                } catch (IOException ex) {
                  ex.printStackTrace();
                } catch (IllegalConnectorArgumentsException ex) {
                  ex.printStackTrace();
                } catch (VMStartException ex) {
                  ex.printStackTrace();
                }
        }

        if (vm == null) {
            throw new RuntimeException("Couldn't start a VM");
        }

        //Stuff to add before the VM startsup
        startupEventRequests();

        vm.resume(); //Only to grab the VMStart Event.
        try {
            eventIterator = vm.eventQueue().remove().eventIterator();
            while(eventIterator.hasNext()){
                Event event = eventIterator.nextEvent();
                if (event instanceof VMStartEvent) {
                    mainThread = ((VMStartEvent)event).thread();
                    VMStarted();
                    return;
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    /**
      * Adds EventRequests to the VM on VM startup.
      */
    public void startupEventRequests(){
        //Lets listen for a step...
        MethodEntryRequest mer = getVirtualMachine().eventRequestManager().createMethodEntryRequest();
        mer.addClassFilter(triggerclassname);
        mer.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        mer.enable();
        
        addVMEventListener(mer, new EventAction(){
            public Graph doAction(Event event) {
                MethodEntryEvent mee = (MethodEntryEvent)event;
                System.out.println(event);
                if (mee.method().name().equals(triggermethodname)) {
                    return generateGraph();
                }
                return null;
            }
        });
        
        mer.enable();
    }

    /**
      * Called when the VM has started.
      */
    public void VMStarted(){ }

    /**
      * Generate and return the next graph.
      * Assumes that the VM has already been paused. It will resume the VM
      * and wait for a graph to be generated. The first graph that is generated
      * is returned and the VM is kept in in a suspended state.
      */
    public Graph getNextGraph(){
        /*Graph g = null;
        vm.resume();
        try{
            boolean vmAlive = true;
            while(vmAlive){
                EventIterator e = vm.eventQueue().remove().eventIterator();
                while(e.hasNext()){
                    Event event = e.nextEvent();
                    if (event instanceof VMDeathEvent) {
                        vmAlive = false;
                    }else if (event instanceof MethodEntryEvent){
                        MethodEntryEvent mee = (MethodEntryEvent)event;
                        if (mee.method().name().contains(triggermethodname)) {
                            g = generateGraph();
                        }
                    }else{
                        System.err.println("Strange event: " + event.getClass().getName());
                    }
                    if (vmAlive) {
                        vm.resume();
                    }
                }
            }
        } catch (InterruptedException ex){
          System.err.println("Couldn't wait for the vm to pause.");
          ex.printStackTrace();
        }
        return g;
      */Graph g = null;
        vm.resume();
        try {
            while(g == null){
                if (eventIterator == null || eventIterator.hasNext() == false) {
                    try{eventIterator = vm.eventQueue().remove().eventIterator();}
                    catch(VMDisconnectedException vde){
                        return null;
                    }
                }
                boolean resume = true;
                while(eventIterator.hasNext()){
                    Event event = eventIterator.nextEvent();
                    if ( event instanceof VMStartEvent ){
                        VMStarted();
                    }else if (!(event instanceof VMDeathEvent || event instanceof VMDisconnectEvent ) ) {
                        EventAction action = actions.get(event.request());
                        if (action != null) {
                            g = action.doAction(event);
                            resume = (g == null);
                        }else{
                        }
                    }else{
                      if (event instanceof VMStartEvent) {
                          mainThread = ((VMStartEvent)event).thread();
                          VMStarted();
                      } else if (event instanceof VMDeathEvent){
                          hasDied = true;
                      }
                 }
                if (resume) { vm.resume(); }
              }
            }
        } catch (InterruptedException ex) {
          System.err.println("Couldn't wait for the vm to pause.");
          ex.printStackTrace();
        }
        return g;//*/
    }

/**
  * Generates the current object graph of the target program. Use this
  * method on a VM that is launched but has all threads suspended.
  * @return
  */
    public Graph generateGraph(){
        MutableNode root = new MutableNode();
        root.gnt = new ObjectGraphRoot();

        objectCache.clear();
        ObjectClassType.clearCache();
        for (ThreadReference thread : vm.allThreads()) {
           try {
               if (thread.threadGroup().name().equals("system")) { continue; }
               //System.out.println("   "+thread.name());
               MutableNode threadNode = new MutableNode();
               threadNode.gnt = new ThreadNode(thread);
               
               MutableNode prev = threadNode;
               for (int d = thread.frameCount() - 1; d > 0; d--) {
                   //System.out.println("      "+prev.gnt);
                   MutableNode newFrame = exploreStackFrame(thread.frame(d), d);
                   prev.addChild(newFrame);
                   prev = newFrame;
               }
               root.addChild(threadNode);
           } catch (IncompatibleThreadStateException ex) {
             ex.printStackTrace();
           }
        }
        return new Graph(root);
    }

    private MutableNode exploreStackFrame(StackFrame frame, int d) throws IncompatibleThreadStateException {
        MutableNode stackframe = new MutableNode();
        stackframe.gnt = new StackFrameNode(frame, d);
        //Make sure that we don't forget the "This" object
        ObjectReference thisObject = frame.thisObject();
        if (thisObject != null) {
          MutableNode objectReference = getObjectReference(thisObject);
          stackframe.addChild( objectReference );
        }
        try {
          for (LocalVariable local : frame.visibleVariables()) {//This throws AbsentInformationException
            if (local == null) { continue; }
            Value val = frame.getValue(local);
            if (val != null && val.type() != null) {
              MutableNode mn = valueCache.getNode(val);
              /*LinkedList<Node> l = new LinkedList<Node>();
              l.add(mn);
              LinkedList<Node> temp = new LinkedList<Node>();
              String space = "         ";
              while(!l.isEmpty()){
                for(int i = 0; i < l.size(); i++){
                 System.out.println(space + l.get(i).gnt);
                 for(Node node : l.get(i).getChildren()){
                    temp.add(node);  
                 }
                }
                l =temp;
                temp = new LinkedList<Node>();
                space += "   ";
              }*/
              stackframe.addChild(mn);
            }
          }
        } catch (AbsentInformationException ex) {
          //TODO: Figure out what can be done here instead of just silencing
          //this exception
        }
        return stackframe;
    }

    /**
* Make sure that we avoid cycles and only explore each object once
* @param thisObject
* @return
*/
    private MutableNode getObjectReference(ObjectReference thisObject) {
      if (!objectCache.containsKey(thisObject.uniqueID())) {
        MutableNode node = new MutableNode();
        node.gnt = new ObjectNode(thisObject);
        objectCache.put(thisObject.uniqueID(), node);
      }
      return objectCache.get(thisObject.uniqueID());
    }

    /**
* Adds an event listener that goes off when the event has been detected.
*/
    public void addVMEventListener(EventRequest e, EventAction ea){
        actions.put(e, ea);
    }


    public VirtualMachine getVirtualMachine() {
        return vm;
    }

    public ThreadReference getMainThread() {
        return mainThread;
    }

    public boolean hasDied(){
        return hasDied;
    }
}

