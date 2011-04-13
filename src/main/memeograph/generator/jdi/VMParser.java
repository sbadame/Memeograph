package memeograph.generator.jdi;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import memeograph.generator.jdi.nodes.*;

import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import memeograph.Config;
import memeograph.graph.Graph;
import memeograph.graph.MutableNode;
import memeograph.graph.Node;
import memeograph.renderer.processing.NodeGraphicsInfo;

/**
 * Does the work of actually listening for when memeograph.step() is called
 * and then calling for an interrogation of the current object graph.
 */
public class VMParser {

  final VirtualMachine virtualMachine;
  private HashMap<Long, MutableNode> objectCache = new HashMap<Long, MutableNode>();
  private ValueNodeCreator valueCache = new ValueNodeCreator();
  private String triggermethodname = "";
  private String triggerclassname = "";

  private ArrayList<Graph> graphList = new ArrayList<Graph>();

  public VMParser(VirtualMachine vm){
    this.virtualMachine = vm;
    String t = Config.getConfig().getProperty(Config.TRIGGER);
    triggermethodname = t.substring(t.lastIndexOf('.')+1);
    triggerclassname = t.substring(0, t.lastIndexOf('.'));
  }

  public Iterator<Graph> getGraphs(){
    if (graphList.isEmpty()) {
      graphList = createGraphs();
    }
    return graphList.iterator();
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
    root.gnt = new ObjectGraphRoot();

    objectCache.clear();
    valueCache.clear();
    ObjectClassType.clearCache();
    for (ThreadReference thread :  virtualMachine.allThreads()) {
      try {
        if (thread.threadGroup().name().equals("system")) { continue; }

        MutableNode threadNode = new MutableNode();
        threadNode.gnt = new ThreadNode(thread);

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
      stackframe.gnt = new StackFrameNode(frame, d);

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
      node.gnt = new ObjectNode(thisObject);
      objectCache.put(thisObject.uniqueID(), node);
    }
    return objectCache.get(thisObject.uniqueID());
  }

}