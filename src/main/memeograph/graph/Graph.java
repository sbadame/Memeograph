package memeograph.graph;

import java.io.Serializable;
import java.util.Iterator;

/**
 * A very simple Graph that holds a single root node.
 * This can return it's root, and offer a preorderTraversal of all of the
 * graph elements. If the graph has a cycle in it, then the traversal will
 * never end. If you need to traverse a graph with cycles (As memeograph often
 * does) then check out ACyclicIterator in the memeograph.util package.
 */
public class Graph implements Serializable {
  private Node root;

  public Graph(Node root){
    this.root = root;
  }

  public Node getRoot(){
    return root;
  }

  /**
   Warning this will run to infinity if there is a cycle in the graph.
   @return
   */
  public Iterator<Node> preorderTraversal(){
    return new GraphIterator(root);
  }

}
