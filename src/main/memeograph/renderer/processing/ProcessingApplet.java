package memeograph.renderer.processing;

import java.util.*;
import processing.core.*;
import memeograph.generator.jdi.nodes.*;
import memeograph.renderer.processing.ui.*;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import memeograph.Config;
import memeograph.graph.Graph;
import memeograph.graph.Node;
import memeograph.util.ACyclicIterator;

/**
 * Does the actual drawing of graphs and nodes. No layout calculation code is
 * in here, checkout GrapLayoutHandler for that.
 * We also do the user input handling here.
 */
public class ProcessingApplet extends PApplet implements MouseWheelListener{

    //Just to have something and avoid the dreaded null
    ArrayList<Graph> graphs = new ArrayList<Graph>();
    LinkedList<Graph> layoutqueue = new LinkedList<Graph>();
    Graph currentgraph = null;

    PFont font3D;
    private final String rendertype;

    //Text Rendering info
    private final int renderfrontback = 1;
    private final int rendertopbottom = 2;
    private int rendermode = renderfrontback;

    private volatile boolean isSetup = false;

    private UI ui = createUI();
    private CameraHandler cameraHandler = new CameraHandler(this);

    public ProcessingApplet(){
        addMouseWheelListener(this);
        if (Config.getConfig().isSwitchSet(Config.USE_OPENGL, false)){
            rendertype = OPENGL;
        }else{
            rendertype = P3D;
        }
    }

    @Override
    public void setup(){
        size(width, height, rendertype); //THIS MUST BE THE FIRST LINE OF CODE
                                     //NO REALLY, IF IT ISN'T THEN PROCESSING'S
                                     //REFELECTION KILLS OPENGL AND YOUR DREAMS
        background(102);
        frame.setResizable(true);

        smooth();
        font3D = createFont("SansSerif.bold", 18);
        textFont(font3D);
        textAlign(CENTER, CENTER);

        cameraHandler.setup();
        ui.init();

        //Setup the UI
        isSetup = true;
    }
    
    @Override
    public void draw(){
        background(102);
        hint(ENABLE_DEPTH_TEST);
        textAlign(CENTER, CENTER);
        cameraHandler.draw();

        boolean havegraph = currentgraph != null
                            && currentgraph.getRoot().lookup(GraphLayoutHandler.class).isLayoutDone();

        if (havegraph) {
            //Now draw the lines between the nodes
            ACyclicIterator<Node> i = new ACyclicIterator<Node>(currentgraph.preorderTraversal());
            while( i.hasNext()){
                Node parent = i.next();
                for (Node kid : parent.getChildren()) {
                    drawLine(parent, kid);
                }
            }

            //And now to actually draw the nodes
            ACyclicIterator<Node> j = new ACyclicIterator<Node>(currentgraph.preorderTraversal());
            while(j.hasNext()) {
                drawNode(j.next());
            }


            //Removing the loading graphic...
            //Total hack but I'm not gonna add more code than needed for this
            if (!ui.getCenter().isEmpty()) { ui.getCenter().clear(); }
        }

        ui.draw();
    }



    private void drawLine(Node f, Node t){
        pushStyle();
        if (f.lookup(GraphNodeType.class) instanceof ObjectGraphRoot) { return; }
        NodeGraphicsInfo from = f.lookup(NodeGraphicsInfo.class);
        NodeGraphicsInfo to = t.lookup(NodeGraphicsInfo.class);

        strokeWeight(5);
        stroke(1f,Math.min(from.opacity, to.opacity));
        line(from.x, from.y, from.z, to.x, to.y, to.z);
        popStyle();
    }

    private void drawNode(Node node){
        if (node.lookup(GraphNodeType.class) instanceof ObjectGraphRoot) { return; }
        pushMatrix(); pushStyle();
        NodeGraphicsInfo n = node.lookup(NodeGraphicsInfo.class);
        GraphNodeType t = node.lookup(GraphNodeType.class);

        translate(n.x, n.y, n.z);

        fill(n.r, n.g, n.b, n.opacity);
        strokeWeight(1);
        box(n.width, 20f, 20f);

        float size = 0;
        String data = null;

        if ((rendermode & renderfrontback) != 0) {
            data = t.toString();
            size = textWidth(data);

            pushMatrix();
            translate(0f, 0f, 11f);
            fill(5);
            text(data, 0, 0f);

            translate(0f, 0f, -22f);

            rotateY(PI);
            text(data, 0f, 0f);
            popMatrix();
        }

        if ((rendermode & rendertopbottom) != 0) {
            if (data == null){data = t.toString(); size = textWidth(data);}

            translate(0f, 11f, 0f);
            fill(5);
            rotateX(-PI/2);
            text(t.toString(), -size/2, 0f);
            rotateX(PI/2);

            translate(0f, -22f, 0f);

            rotateX(-PI/2);
            rotateY(PI);
            textAlign(LEFT);
            text(t.toString(), -size/2, 0f);
        }
        popMatrix(); popStyle();
    }
    
    public void addGraph(Graph newGraph){
        GraphLayoutHandler layout = new GraphLayoutHandler(newGraph, this);
        newGraph.getRoot().store(GraphLayoutHandler.class, layout);

        if (!isSetup) {
            layoutqueue.add(newGraph);
            return;
        }

        while(!layoutqueue.isEmpty()){
            Graph graph = layoutqueue.pop();
            graph.getRoot().lookup(GraphLayoutHandler.class).doLayout();
            graphs.add(graph);
            if (currentgraph == null) { currentgraph = graph; }
        }

        layout.doLayout();
        graphs.add(newGraph);
        if (currentgraph == null) { currentgraph = newGraph; }
    }

    /**
     * This is called when the user wants to see the next graph
     */
    public void showNextGraph(){
      int i = graphs.indexOf(currentgraph);
      if (i >= graphs.size() - 1) {
        System.err.println("No more to show you...");
        return;
      }else{
        currentgraph = graphs.get(i + 1);
      }
    }


    @Override
    public void mouseDragged()
    {
        if (currentgraph == null) { return; }
        cameraHandler.mouseDragged();
    }

    @Override
    public void keyPressed(){
        if (currentgraph == null) { return; }
        cameraHandler.keyPressed();
        char k = (char)key;
        switch(k){
            case 't':
            case 'T': toggleRenderMode(); break;
            case 'n':
            case 'N': showNextGraph(); break;
        }
    }

    private void toggleRenderMode(){
        rendermode = (rendermode + 1) % ((renderfrontback|rendertopbottom) + 1);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        cameraHandler.mouseWheelMoved(e);
    }

    public ArrayList<Graph> getGraphs() {
        return graphs;
    }

    public Graph getCurrentGraph(){
        return currentgraph;
    }

    protected UI createUI(){
        return new UI(this);
    }
}