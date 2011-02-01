package memeograph.graph;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

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

  protected HashMap<Class, Object>  hashMap = new HashMap<Class, Object> ();
  protected Collection<Node> children = new LinkedList<Node>();

  public Collection<Node> getChildren(){
    return children;
  }

  public boolean hasChildren() {
    return !children.isEmpty();
  }

  @SuppressWarnings("unchecked")
  public <E> E lookup(Class<E> key){
    Object val = hashMap.get(key);
    if (key.isInstance(val)) { return (E) val; }
    return null;
  }

  public <E> void store(Class<E> key, E val){
    hashMap.put(key, val);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Node other = (Node) obj;
    if (this.hashMap != other.hashMap && (this.hashMap == null || !this.hashMap.equals(other.hashMap))) {
      return false;
    }
    if (this.children != other.children && (this.children == null || !this.children.equals(other.children))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 71 * hash + (this.hashMap != null ? this.hashMap.hashCode() : 0);
    hash = 71 * hash + (this.children != null ? this.children.hashCode() : 0);
    return hash;
  }

  public Color getColor() {
    if (hashMap.containsKey(Color.class)) return lookup(Color.class);
    return null;
  }

  @Override
  public String toString(){
    if (hashMap.containsKey(String.class)) { return lookup(String.class); }
    return super.toString();
  }
}