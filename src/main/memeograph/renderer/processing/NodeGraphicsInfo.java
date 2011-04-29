package memeograph.renderer.processing;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import memeograph.generator.jdi.nodes.GraphNodeType;
import memeograph.graph.Node;

/**
 * Store the information that we need to draw this node.
 */
public class NodeGraphicsInfo implements Serializable{
    public float x, y, z;
    public int r,g,b;
    public Node node;

    public float opacity = 255;
    public float width = 70;
    protected int childcount = 0;

    public GraphLayoutHandler glh;
    public GraphNodeType gnt;
    protected LinkedList<NodeGraphicsInfo> children = new LinkedList<NodeGraphicsInfo>();

    public NodeGraphicsInfo(Color c,Node n){
        node = n;
        this.gnt = n.gnt;
        Random rand = new Random();
        if (null == c) {
          r = 200; g = 200; b = 245;
        } else {
          r = c.getRed(); g = c.getGreen(); b = c.getBlue();
        }

        r = rand.nextInt(20) + (r - 10);
        g = rand.nextInt(20) + (g - 10);
        b = rand.nextInt(10) + (b - 5);

        r = r < 255 ? (r > 0 ? r : 0) : 255;
        g = g < 255 ? (g > 0 ? g : 0) : 255;
        b = b < 255 ? (b > 0 ? b : 0) : 255;
    }

    public Collection<NodeGraphicsInfo> getChildren(){
      return children;
    }

    public void addChild(NodeGraphicsInfo n){
      if (n == null) {
        throw new NullPointerException();
      }
      childcount++;
      children.add(n);
    }
    
    public Coordinate getCoordinate()
    {
        return new Coordinate(x,y,z);
    }
}
