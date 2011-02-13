package memeograph;

import memeograph.graph.Graph;


/**
 * Takes a set of object graphs and render them for viewers to enjoy
 */
public interface Renderer {

    /*
     * This is called at the start of the running of the program. The idea is
     * that if the Renderer is a GUI of some sort, it can popup at the very
     * start, before the builder has been created and is working.
     */
    public void init();

    /**
     * Adds a graph to the renderer
     * @param graph The graph to be added
     */
    public void addGraph(Graph graph);

    public void finish();
}