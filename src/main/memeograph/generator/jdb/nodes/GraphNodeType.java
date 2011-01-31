package memeograph.generator.jdb.nodes;

import java.io.Serializable;

/**
 * Holds information about what kind of node every node in a graph actually
 * is. Every node in a graph has a type associated with it. The only requirement
 * is that every type have some sort of unique ID that no other node of any
 * type have.
 *
 * toString() methods of Types should be human readable for the most part
 * Renderers should use them to retrieve that text that represents a node.
 */
public interface GraphNodeType extends Serializable{

  public String getUniqueID();
}
