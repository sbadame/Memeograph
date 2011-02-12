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

import memeograph.GraphGenerator;
import memeograph.Config;
import memeograph.graph.Graph;
import memeograph.graph.MutableNode;

/**
 * This will interrogate a VM for an object graph whenever memeograph.step()
 * is called in the SuT.
 *
 * Has the slightly strange side effect that if "jdbgrapher.savefile" is set it
 * will serialize the graph and save it to the filename pointed to by the property.
 * that graph can then be loaded using the GraphFileLoader which is much faster
 * then re-running the program and building the graph from scratch in most
 * cases.
 */
public class JDBGraphGenerator implements GraphGenerator {

  protected VirtualMachine virtualMachine;
  protected Config config;

  private final HashMap<Long, MutableNode> objectCache = new HashMap<Long, MutableNode>();
  private final ValueNodeCreator valueCache = new ValueNodeCreator();
  private final String triggermethodname;
  private final String triggerclassname;
  private ArrayList<Graph> graphList = new ArrayList<Graph>();

  private String target;
  private String target_args;

  public JDBGraphGenerator(Config config){
      this.config = config;
      target_args = config.getProperty(Config.VM_OPTIONS, "");
      target = target_args.substring(target_args.lastIndexOf(' '));
      target_args = target_args.substring(0, target_args.lastIndexOf(' '));

      String t = Config.getConfig().getProperty(Config.TRIGGER);
      triggermethodname = t.substring(t.lastIndexOf('.')+1);
      triggerclassname = t.substring(0, t.lastIndexOf('.'));
  }

  @Override
  public void start(){
      virtualMachine = launchTargetVM(target, target_args);
  }

  @Override
  public Iterator<Graph> getGraphs() {
      graphList = createGraphs();
      return graphList.iterator();
  }

  private static VirtualMachine launchTargetVM(String main_command, String vm_options){
      for (LaunchingConnector connector : Bootstrap.virtualMachineManager().launchingConnectors()) {
              if (connector.transport().name().equals("dt_socket") == false) {
                  continue;
              }
              Map<String, Argument> launchargs = connector.defaultArguments();
              launchargs.get("options").setValue(vm_options);
              launchargs.get("main").setValue(main_command);
              try {
                  final VirtualMachine vm = connector.launch(launchargs);
                  new ProcessDirector(vm.process()).start();
                  return vm;
              } catch (IOException ex) {
                ex.printStackTrace();
              } catch (IllegalConnectorArgumentsException ex) {
                ex.printStackTrace();
              } catch (VMStartException ex) {
                ex.printStackTrace();
              }
      }
      return null;
  }

  public ArrayList<Graph> createGraphs(){

      ArrayList<Graph> graphs = new ArrayList<Graph>();
      virtualMachine.resume();

      //Lets listen for a step...
      MethodEntryRequest mer = virtualMachine.eventRequestManager().createMethodEntryRequest();
      mer.addClassFilter(triggerclassname);
      mer.setSuspendPolicy(EventRequest.SUSPEND_ALL);
      mer.enable();

      try {
        boolean vmAlive = true;
        while(vmAlive){
          EventIterator e = virtualMachine.eventQueue().remove().eventIterator();
          while(e.hasNext()){
            Event event = e.nextEvent();
            if (event instanceof VMDeathEvent) {
                vmAlive = false;
            }else if (event instanceof MethodEntryEvent){
                MethodEntryEvent mee = (MethodEntryEvent)event;
                if (mee.method().name().contains(triggermethodname)) {
                  graphs.add(interrogate());
                }
            }else if (event instanceof VMStartEvent){
              //Meh, who cares... the VM is starting... Parade?
            }else{
              System.err.println("Strange event" + event.getClass().getName());
            }
            if (vmAlive) {
              virtualMachine.resume();
            }
          }
        }
      } catch (InterruptedException ex) {
        System.err.println("Couldn't wait for the vm to pause.");
        ex.printStackTrace();
      }
      return graphs;
  }


    //Assumes that the VM is stopped. Then goes in an builds a graph!
    //It returns a single snapshot of the program
    private Graph interrogate(){
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

}
