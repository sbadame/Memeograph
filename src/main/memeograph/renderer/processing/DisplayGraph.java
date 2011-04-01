/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memeograph.renderer.processing;

import java.util.Iterator;

/**
 *
 * @author mwaldron74
 */
public class DisplayGraph{
    private NodeGraphicsInfo root;
    public DisplayGraph(NodeGraphicsInfo root){
      this.root = root;
    }
    public NodeGraphicsInfo getRoot(){
      return root;
    }
    public Iterator<NodeGraphicsInfo> preorderTraversal(){
      return new DisplayGraphIterator(root);
    }
}
