package memeograph.graph;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import memeograph.generator.jdi.nodes.GraphNodeType;
import memeograph.renderer.processing.GraphLayoutHandler;

/**
 * A Graph is a collection of connected Nodes.
 * Nodes usually represent some sort of data, the trouble is that to
 * different parts of memeograph Nodes represent different things.
 * (To the generator a node represents an object, to a renderer a node represents
 * something to draw)
 *
 * To allow for different systems to "look" at Nodes as they deem relevant. A
 * system has been implemented that allows for Objects to be tacked onto Nodes
 * through the lookup/store methods.
 *
 * store(Class<E>, E) allows you to store an object associated with that class
 * in this Node.
 * lookup(Class<E>) allows you retrieve that Object E.
 *
 * For example: JDBGrapher stores in every node a GraphNodeType object. So to find out what
 * what a node represents in the graph simply use: lookup(GraphNodeType.class) to retrieve
 * a Type object and figure out what the node represents.
 *
 * The Processing renderer uses NodeGraphicsInfo to store the graphical details
 * of the node.
 */
public class Node implements Serializable{
  public GraphNodeType gnt;
  protected Collection<Node> children = new LinkedList<Node>();

  public Collection<Node> getChildren(){
    return children;
  }

  public boolean hasChildren() {
    return !children.isEmpty();
  }
  
  public Color getColor() {
    return null;
  }
}