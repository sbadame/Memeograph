package memeograph;

import java.util.Iterator;
import memeograph.graph.Graph;


/**
 * Takes a set of object graphs and render them for viewers to enjoy
 */
public interface GraphRenderer {

    /*
     * This is called at the start of the running of the program. The idea is
     * that if the GraphRenderer is a GUI of some sort, it can popup at the very
     * start, before the builder has been created and is working.
     */
    public void init();

    /*
     * This will be called when a graph has been created by the builder
     * and wishes to be displayed
     */
    public void setGraphs(Iterator<Graph> graphs);
}
