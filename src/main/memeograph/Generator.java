package memeograph;

import java.util.Iterator;
import memeograph.graph.Graph;

/**
 *  Creates an sequence of graphs that the GraphRenderer will be responsible to
 *  to display.
 *
 *  start() will be called after the GraphRenderer has already had init() called.
 *  Once getGraphs() returns, the GraphRenderer can get to work.
 *
 *  getGraphs() is not allowed to return null.
 */
public interface Generator {
  public void start();
  public boolean isAlive();
  public Graph getNextGraph();
}
