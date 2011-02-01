package memeograph.renderer.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import memeograph.generator.jdb.nodes.ObjectClassType;
import memeograph.generator.jdb.nodes.ObjectNode;
import memeograph.generator.jdb.nodes.StackFrameNode;
import memeograph.generator.jdb.nodes.ThreadNode;
import memeograph.generator.jdb.nodes.GraphNodeType;
import memeograph.graph.Graph;
import memeograph.graph.Node;
import processing.core.PApplet;

/**
 * Takes a graph and generates the x/y/z coordinates for each of its Nodes.
 * Those coordinates are then stored in the node in a NodeGraphicsInfo Object.
 */
public class GraphLayoutHandler {
  public static final int PADDING = 10;

  private final Graph g;
  private final PApplet applet;


  //The Grid!!!
  //Lookup the node stored on an (y,z) rail
  private HashMap<Integer, HashMap<Integer, ArrayList<Node>>> grid =
          new HashMap<Integer, HashMap<Integer, ArrayList<Node>>>();


  public GraphLayoutHandler(Graph g, PApplet applet){
    this.g = g;
    this.applet = applet;
  }

  public void doLayout(){
      for (Node thread : g.getRoot().getChildren()) {
        assert(thread.lookup(GraphNodeType.class) instanceof ThreadNode); //Sanity check
        layout(thread, -10, 0);

        if(!thread.hasChildren()) continue;

        Node sf = thread;
        int y = 0;
        HashSet<Node> seen = new HashSet<Node>();
        do{
            sf = thread.getChildren().iterator().next();
            if (seen.contains(sf)) break;
            y += 1;
            layout(sf, -10, y);
            seen.add(sf);
        }while(sf.hasChildren());
      }
      setXPositions(g);
  }

    private void layout(Node n, int z, int y)
    {
        NodeGraphicsInfo newInfo = new NodeGraphicsInfo(n.lookup(GraphNodeType.class).getColor());
        newInfo.x = 0; //We don't know X yet, we have to go back and add it later
        newInfo.y = y * 50;
        newInfo.z = z * 50;
        newInfo.width = applet.textWidth(n.lookup(GraphNodeType.class).toString());
        addToGrid(n, y, z);

        n.store(NodeGraphicsInfo.class, newInfo);

        for (Node child : n.getChildren()) {
           assert(child != null);
           if (isZChild(n, child)) {
             layout(child, z-1, y);
           }else{
             layout(child, z, y+1);
           }
        }
    }

    private void addToGrid(Node n, int y, int z) {
        if (!grid.containsKey(z)) {
            grid.put(z, new HashMap<Integer, ArrayList<Node>>());
        }
        HashMap<Integer, ArrayList<Node>> zPlane = grid.get(z);

        if (!zPlane.containsKey(y)){
            zPlane.put(y, new ArrayList<Node>());
        }

        ArrayList<Node> rail = zPlane.get(y);
        rail.add(n);
    }

    private void setXPositions(Graph g){
        HashSet<Node> seen = new HashSet<Node>();
        for (Integer y : grid.keySet()) {
            //Go down the ladder...
            HashMap<Integer, ArrayList<Node>> zPlane = grid.get(y);
            for (Integer z : zPlane.keySet()) {
                float xPosition = 0;
                ArrayList<Node> spike = zPlane.get(z);
                for (Node node : spike) {
                   if (seen.contains(node)) { continue; }
                   NodeGraphicsInfo ngi = node.lookup(NodeGraphicsInfo.class);
                   ngi.x = xPosition;
                   seen.add(node);
                   xPosition += (xPosition == 0 ? ngi.width/2 : ngi.width);
                   xPosition += PADDING*10;
                }
            }
        }
    }

    //A Z child goes into the plane...
    public boolean isZChild(Node parent, Node child){
      GraphNodeType p = parent.lookup(GraphNodeType.class);
      GraphNodeType c = child.lookup(GraphNodeType.class);
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

}
