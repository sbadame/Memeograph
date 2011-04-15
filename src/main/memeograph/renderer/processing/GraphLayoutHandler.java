package memeograph.renderer.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import memeograph.generator.jdi.nodes.ObjectClassType;
import memeograph.generator.jdi.nodes.ObjectNode;
import memeograph.generator.jdi.nodes.StackFrameNode;
import memeograph.generator.jdi.nodes.ThreadNode;
import memeograph.generator.jdi.nodes.GraphNodeType;
import processing.core.PApplet;

/**
 * Takes a graph and generates the x/y/z coordinates for each of its Nodes.
 * Those coordinates are then stored in the node in a NodeGraphicsInfo Object.
 */
public class GraphLayoutHandler {
  public static final int PADDING = 80;

  //private final Graph g;
  private final DisplayGraph dg;
  private final PApplet applet;
  private boolean didLayout = false;


  //The Grid!!!
  //Lookup the node stored on an (y,z) rail
  private HashMap<Integer, HashMap<Integer, ArrayList<NodeGraphicsInfo>>> grid =
          new HashMap<Integer, HashMap<Integer, ArrayList<NodeGraphicsInfo>>>();


  public GraphLayoutHandler(DisplayGraph graph, PApplet applet){
    dg = graph;
    this.applet = applet;
  }

    private void layout(NodeGraphicsInfo n, int z, int y)
    {
        n.x = 0; //We don't know X yet, we have to go back and add it later
        n.y = y * 50;
        n.z = z * 50;
        if(n.gnt != null && (n.width = applet.textWidth(n.gnt.toString())) < 150)
          n.width = 150f;
          
        addToGrid(n, y, z);

        for (NodeGraphicsInfo child : n.getChildren()) {
           assert(child != null);
           if (isZChild(n, child)) {
             layout(child, z-1, y);
           }else{
             layout(child, z, y+1);
           }
        }
    }

    public void doLayout(){
      ThreeDTree tdt = new ThreeDTree(dg.getRoot());
      for (NodeGraphicsInfo thread : dg.getRoot().getChildren()) {
        assert(thread.gnt instanceof ThreadNode);
        layout(thread, -10, 0);

        if(!thread.getChildren().isEmpty()) continue;

        NodeGraphicsInfo sf = thread;
        int y = 0;
        HashSet<NodeGraphicsInfo> seen = new HashSet<NodeGraphicsInfo>();

        while(!sf.getChildren().isEmpty()){
            sf = thread.getChildren().iterator().next();
            if (seen.contains(sf)) break;
            y += 1;
            layout(sf, -10, y);
            seen.add(sf);
        }
      }

      setXPositions();
      didLayout = true;
    }

    private void addToGrid(NodeGraphicsInfo n, int y, int z) {
        if (!grid.containsKey(z)) {
            grid.put(z, new HashMap<Integer, ArrayList<NodeGraphicsInfo>>());
        }
        HashMap<Integer, ArrayList<NodeGraphicsInfo>> zPlane = grid.get(z);
        if (!zPlane.containsKey(y)){
            zPlane.put(y, new ArrayList<NodeGraphicsInfo>());
        }
        ArrayList<NodeGraphicsInfo> rail = zPlane.get(y);
        rail.add(n);
    }

    private void setXPositions(){
        HashSet<NodeGraphicsInfo> seen = new HashSet<NodeGraphicsInfo>();
        float mid = findMid();
        float xPos = 0;
        float width = 0;
        for( Integer y : grid.keySet()){
            HashMap<Integer, ArrayList<NodeGraphicsInfo>> zPlane = grid.get(y);
            for(Integer z : zPlane.keySet()){
                width = 0;
                for(int i = 0; i < zPlane.get(z).size(); i++){
                    NodeGraphicsInfo node = zPlane.get(z).get(i);
                    if(seen.contains(node)){ continue; }
                    width += 150 + PADDING;
                    seen.add(node);
                }
                xPos = mid - (width/2);
                seen = new HashSet<NodeGraphicsInfo>();
                for(int i = 0; i < zPlane.get(z).size(); i++){
                    NodeGraphicsInfo node = zPlane.get(z).get(i);
                    if (seen.contains(node)) { continue; }
                    node.x = xPos + PADDING;
                    seen.add(node);
                    xPos += node.width + PADDING;
                }
            }
        }
    }
    private float findMid(){
      float max=0;
      float curmax;
      for(Integer y : grid.keySet()){
        HashMap<Integer, ArrayList<NodeGraphicsInfo>> zPlane = grid.get(y);
        curmax = 0;
        for(Integer z : zPlane.keySet()){
          for(NodeGraphicsInfo node : zPlane.get(z)){
            curmax += node.width;
          }
          if(curmax > max)
            max = curmax;
        }
      }
      return max/2;
    }
    //A Z child goes into the plane...
    public static boolean isZChild(NodeGraphicsInfo parent, NodeGraphicsInfo child){
      GraphNodeType p = parent.node.gnt;
      GraphNodeType c = child.node.gnt;
      if ((p instanceof ThreadNode || p instanceof StackFrameNode) && (c instanceof StackFrameNode) ) {
        return false;
      }else if (p instanceof StackFrameNode){
        return true;
      }else if (c instanceof ObjectNode && p instanceof ObjectNode){
        ObjectClassType parentType = ((ObjectNode)p).type;
        ObjectClassType childType = ((ObjectNode)c).type;
        return parentType.equals(childType) || childType.isSubclassOf(parentType);
      }
      return false;
    }
    public boolean isLayoutDone() {
      return didLayout;
    }
}
