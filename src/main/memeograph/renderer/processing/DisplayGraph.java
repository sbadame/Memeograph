package memeograph.renderer.processing;

import java.util.Iterator;
import memeograph.util.ACyclicIterator;

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
    public boolean contains(NodeGraphicsInfo ngi){
        ACyclicIterator<NodeGraphicsInfo> aci;
        aci = new ACyclicIterator<NodeGraphicsInfo>(preorderTraversal());
        while(aci.hasNext())
            if(aci.next().equals(ngi))
                return true;
        return false;
    }
    public NodeGraphicsInfo ngiThisGraph(NodeGraphicsInfo ngi){
        ACyclicIterator<NodeGraphicsInfo> aci;
        aci = new ACyclicIterator<NodeGraphicsInfo>(preorderTraversal());
        while(aci.hasNext()){
            NodeGraphicsInfo other = aci.next();
            if(other.equals(ngi))
                return other;
        }
        return null;
    }
}
