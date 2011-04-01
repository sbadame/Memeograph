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
    private static final int animationCountMax = 20;
    private static final float OPACITY_COUNT = ((255.0f)/animationCountMax);
    private int animationCount = animationCountMax;
    private int currentgraphindex = 0;

    //Just to have something and avoid the dreaded null
    ArrayList<DisplayGraph> dgraphs = new ArrayList<DisplayGraph>();
    LinkedList<DisplayGraph> layoutqueue = new LinkedList<DisplayGraph>();
    DisplayGraph currentgraph;

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

        if (currentgraph == null) { return; }

        //Slowly Fade away old graph then fade in new graph.
        if (animationCount == animationCountMax - 1 && currentgraphindex < dgraphs.size() - 1){
            currentgraph=dgraphs.get(currentgraphindex + 1);
            currentgraphindex++;
            
            ACyclicIterator<NodeGraphicsInfo> k;
            k=new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
            while(k.hasNext()){
              NodeGraphicsInfo node = k.next();
              node.opacity = 0;
            }
            animationCount--;
        }

        //animate nodes
        if (animationCount < animationCountMax - 1 && currentgraphindex < dgraphs.size()){
          ACyclicIterator<NodeGraphicsInfo> k;
          k = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
          while(k.hasNext()){
            NodeGraphicsInfo node = k.next();
            if(currentgraphindex > 0 //&&
                     /*hasNode(dgraphs.get(currentgraphindex-1),node) == null*/){
              node.opacity+=OPACITY_COUNT;
            }
          }
          animationCount--;
        }

        //end of animation...reset animation count
        if(animationCount == 0){
           animationCount = animationCountMax;
        }

        //Now draw the lines between the nodes
        ACyclicIterator<NodeGraphicsInfo> i = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
        while( i.hasNext()){
          NodeGraphicsInfo parent = i.next();
          for (NodeGraphicsInfo kid : parent.getChildren()) {
            drawLine(parent, kid);
          }
        }
        //And now to actually draw the nodes
        ACyclicIterator<NodeGraphicsInfo> j = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
        while(j.hasNext()) {
          drawNode(j.next());
        }
        //Removing the loading graphic...
        //Total hack but I'm not gonna add more code than needed for this
        if (!ui.getCenter().isEmpty()) { ui.getCenter().clear(); }

        ui.draw();
    }



    private void drawLine(NodeGraphicsInfo f, NodeGraphicsInfo t){
        pushStyle();
        if (f.gnt instanceof ObjectGraphRoot) { return; }
        NodeGraphicsInfo from = f;
        NodeGraphicsInfo to = t;

        strokeWeight(5);
        stroke(1f,Math.min(from.opacity, to.opacity));
        line(from.x, from.y, from.z, to.x, to.y, to.z);
        popStyle();
    }

    private void drawNode(NodeGraphicsInfo node){
        if (node.gnt instanceof ObjectGraphRoot) { return; }
        pushMatrix(); pushStyle();
        NodeGraphicsInfo n = node;
        GraphNodeType t = node.gnt;

        translate(n.x, n.y, n.z);

        fill(n.r, n.g, n.b, n.opacity);
        strokeWeight(1);
        box(n.width, 20f, 20f);

        float size = 0;
        String data = null;

        if ((rendermode & renderfrontback) != 0) {
            try{
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
            }catch(NullPointerException npe){
              
          }
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

    private DisplayGraph displayGraph(Graph graph){
      NodeGraphicsInfo root = new NodeGraphicsInfo(null,graph.getRoot());
      LinkedList<Node> temp;
      LinkedList<NodeGraphicsInfo> ngitemp;
      LinkedList<Node> list = new LinkedList<Node>();
      LinkedList<NodeGraphicsInfo> ngilist = new LinkedList<NodeGraphicsInfo>();
      Node node;
      NodeGraphicsInfo ngi;
      list.add(root.node);
      ngilist.add(root);
      DisplayGraph dg = new DisplayGraph(root);

      while(!list.isEmpty()){
        temp = new LinkedList<Node>();
        ngitemp = new LinkedList<NodeGraphicsInfo>();
        for(int j = 0; j < list.size(); j++){//parents
          node = list.get(j);
          ngi = ngilist.get(j);
          for(Node child : node.getChildren()){//children
            temp.add(child);
            NodeGraphicsInfo ngichild = new NodeGraphicsInfo(null,child);
            ngitemp.add(ngichild);
            ngi.addChild(ngichild);

          }
        }
        list = temp;
        ngilist = ngitemp;
      }
      return dg;
    }

    public void addGraph(Graph newGraph){
        DisplayGraph dg = displayGraph(newGraph);
        GraphLayoutHandler layout = new GraphLayoutHandler(dg, this);
        if (!isSetup) {
            layoutqueue.add(dg);
            return;
        }
        while(!layoutqueue.isEmpty()){
            DisplayGraph graph = layoutqueue.pop();
            graph.getRoot().glh.doLayout();
            dgraphs.add(graph);
            if (currentgraph == null) { currentgraph = graph; }
        }
        layout.doLayout();
        dgraphs.add(dg);
        if (currentgraph == null) { currentgraph = dg; }
    }

    /**
     * This is called when the user wants to see the next graph
     */
    protected void showNextGraph(){
      if (currentgraphindex >= dgraphs.size() - 1) {
        System.err.println("No more to show you...");
        return;
      }else if(animationCount == animationCountMax){
        animationCount--;
      }
    }

    /*
     * This is called when the user wants to see the previous graph
     */
    private void showPrevGraph(){
      if(currentgraphindex <= 0) {
        System.err.println("No more to show you...");
        return;
      }else{
        currentgraph = dgraphs.get(currentgraphindex - 1);
        currentgraphindex--;
        ACyclicIterator<NodeGraphicsInfo> k = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
        while(k.hasNext()){
            NodeGraphicsInfo ngi = k.next();
            if(ngi != null)
               ngi.opacity=255;
        }
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
            case 'p':
            case 'P': showPrevGraph(); break;
        }
    }

    private void toggleRenderMode(){
        rendermode = (rendermode + 1) % ((renderfrontback|rendertopbottom) + 1);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        cameraHandler.mouseWheelMoved(e);
    }

    public ArrayList<DisplayGraph> getGraphs() {
        return dgraphs;
    }

    public ArrayList<DisplayGraph> getDisplayGraphs() {
      return dgraphs;
    }

    public DisplayGraph getCurrentGraph(){
        return currentgraph;
    }

    protected UI createUI(){
        return new UI(this);
    }

    private NodeGraphicsInfo hasNode(DisplayGraph g, NodeGraphicsInfo thisNode){
      ACyclicIterator<NodeGraphicsInfo> k = new ACyclicIterator<NodeGraphicsInfo>(g.preorderTraversal());
      while(k.hasNext()){
        NodeGraphicsInfo graphNode = k.next();
        if(thisNode.equals(graphNode)){
          return thisNode;
        }
      }
      return null;
    }
}