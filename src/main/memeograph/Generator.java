package memeograph;

import java.util.Iterator;

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
  public abstract Iterator<memeograph.graph.Graph> getGraphs();
}
