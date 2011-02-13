package memeograph.generator.jdb;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;

import java.util.*;
import java.io.IOException;

import memeograph.generator.jdb.nodes.*;

import memeograph.Generator;
import memeograph.Config;
import memeograph.graph.Graph;
import memeograph.graph.MutableNode;
import org.w3c.dom.events.EventException;

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
public class JDBGraphGenerator implements Generator {

    protected VirtualMachine virtualMachine;
    protected Config config;

    private final HashMap<Long, MutableNode> objectCache = new HashMap<Long, MutableNode>();
    private final ValueNodeCreator valueCache = new ValueNodeCreator();
    private final String triggermethodname;
    private final String triggerclassname;

    private final String target;
    private final String target_args;
    private boolean isAlive = true;
    private EventIterator eventIterator = null;
    private HashMap<EventRequest, EventAction> actions = new HashMap<EventRequest, EventAction>();

    public JDBGraphGenerator(Config config){
        this.config = config;
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
                    virtualMachine = connector.launch(launchargs);
                    new ProcessDirector(virtualMachine.process()).start();
                    keepgoing = false;
                } catch (IOException ex) {
                  ex.printStackTrace();
                } catch (IllegalConnectorArgumentsException ex) {
                  ex.printStackTrace();
                } catch (VMStartException ex) {
                  ex.printStackTrace();
                }
        }

        if (virtualMachine == null) {
            throw new RuntimeException("Couldn't start a VM");
        }

        //Start up the VM and wait for a graph to be generated
        virtualMachine.resume();

        startupEventRequests();

        try {
            eventIterator = virtualMachine.eventQueue().remove().eventIterator();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    public void startupEventRequests(){
        //Lets listen for a step...
        MethodEntryRequest mer = virtualMachine.eventRequestManager().createMethodEntryRequest();
        mer.addClassFilter(triggerclassname);
        mer.setSuspendPolicy(EventRequest.SUSPEND_ALL);

        addVMEventListener(mer, new EventAction(){
            public Graph doAction(Event event) {
                MethodEntryEvent mee = (MethodEntryEvent)event;
                if (mee.method().name().contains(triggermethodname)) {
                    return generateGraph();
                }
                return null;
            }
        });

        mer.enable();
    }

    /**
     * Generates and returns the next graph
     */
    public Graph getNextGraph(){
        Graph g = null;

        try {
            while(g == null){
                if (eventIterator == null || eventIterator.hasNext() == false) {
                    try{eventIterator = virtualMachine.eventQueue().remove().eventIterator();}
                    catch(VMDisconnectedException vde){
                        isAlive = false;
                        return null;
                    }
                }
                while(eventIterator.hasNext()){
                    Event event = eventIterator.nextEvent();
                    if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
                        isAlive = false;
                    }else if (event instanceof VMStartEvent){
                        //Meh, who cares... the VM is starting... Parade?
                    } else {
                        EventAction action = actions.get(event.request());
                        if (action != null) {
                            g = action.doAction(event);
                        }else{
                            System.err.println("Strange event" + event.getClass().getName());
                        }
                    }
                    if (isAlive) {
                      virtualMachine.resume();
                    }
                }
            }
        } catch (InterruptedException ex) {
          System.err.println("Couldn't wait for the vm to pause.");
          ex.printStackTrace();
        }
        return g;
    }

    public boolean isAlive(){
        return isAlive;
    }

    /**
     * Generates the current object graph of the target program. Use this
     * method on a VM that is launched but has all threads suspended.
     * @return
     */
    public Graph generateGraph(){
        MutableNode root = new MutableNode();
        root.store(GraphNodeType.class, new ObjectGraphRoot());

        objectCache.clear();
        ObjectClassType.clearCache();
        for (ThreadReference thread :  virtualMachine.allThreads()) {
           try {
               if (thread.threadGroup().name().equals("system")) { continue; }

               MutableNode threadNode = new MutableNode();
               threadNode.store(GraphNodeType.class, new ThreadNode(thread));
               MutableNode prev = threadNode;

               for (int d = thread.frameCount() - 1; d > 0; d--) {
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
        stackframe.store(GraphNodeType.class, new StackFrameNode(frame, d));

        //Make sure that we don't forget the "This" object
        ObjectReference thisObject = frame.thisObject();
        if (thisObject != null) {
          MutableNode objectReference = getObjectReference(thisObject);
          stackframe.addChild(  objectReference );
        }
        try {
          for (LocalVariable local : frame.visibleVariables()) { //This throws AbsentInformationException
            if (local == null) { continue; }
            Value val = frame.getValue(local);
            if (val != null && val.type() != null) {
              stackframe.addChild(valueCache.getNode(val));
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
        node.store(GraphNodeType.class, new ObjectNode(thisObject));
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

}
