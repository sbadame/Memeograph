package memeograph;

import memeograph.graph.Graph;

/**
 *  Creates an sequence of graphs that the GraphRenderer will be responsible to
 *  to display.
 *
 *  start() will be called after the GraphRenderer has already had init() called.
 *  Once getGraphs() returns, the GraphRenderer can get to work.
 *
 */
public interface Generator {
  public void start();

  /**
   * Blocks until the next graph is generated. Returns null if there are no
   * more graphs.
   */
  public Graph getNextGraph();
}
