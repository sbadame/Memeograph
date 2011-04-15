/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memeograph.renderer.processing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author peter.boothe
 */
class ThreeDTree {
  public ThreeDTree(NodeGraphicsInfo root) {
    this(root, new HashSet<NodeGraphicsInfo>());
  }

  private ThreeDTree(NodeGraphicsInfo root, Set<NodeGraphicsInfo> seen)
  {
    seen.add(root);

    List<NodeGraphicsInfo> zkids = new ArrayList<NodeGraphicsInfo>();
    List<NodeGraphicsInfo> ykids = new ArrayList<NodeGraphicsInfo>();

    for (NodeGraphicsInfo child : root.getChildren()) {
      if (seen.contains(child)) continue;

      if (GraphLayoutHandler.isZChild(root, child)) {
        zkids.add(child);
      } else {
        ykids.add(child);
      }
    }


  }

}
