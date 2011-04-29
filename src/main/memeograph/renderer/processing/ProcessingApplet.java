package memeograph.renderer.processing;

import java.awt.Color;
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
    private static final int animationCountMax = 50;
    private static final float OPACITY_COUNT = ((255.0f) / (animationCountMax / 2));
    private int animationCount = animationCountMax;
    private int currentgraphindex = 0;
    private HashMap<NodeGraphicsInfo,Coordinate> locationMap;
    private HashMap<NodeGraphicsInfo,Coordinate> tempMap;
    private ArrayList<NodeGraphicsInfo> nextGraphList;
    private ArrayList<ArrayList<Line>> graphLines = new ArrayList<ArrayList<Line>>();
    private ArrayList<DisplayGraph> dgraphs = new ArrayList<DisplayGraph>();
    private LinkedList<DisplayGraph> layoutqueue = new LinkedList<DisplayGraph>();
    private DisplayGraph currentgraph;

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
        
        if(currentgraphindex < dgraphs.size() - 1 && animationCount != animationCountMax)
            animationStep();
        else if (graphLines.size() != 0){
            for(Line line : graphLines.get(currentgraphindex) ){
                line.draw(this);
            }
        }
        //And now to actually draw the nodes
        ACyclicIterator<NodeGraphicsInfo> j = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
        while(j.hasNext()) {
            drawNode(j.next());
        }
        //Removing the loading graphic...
        //Total hack but I'm not gon55na add more code than needed for this
        if (!ui.getCenter().isEmpty()) { ui.getCenter().clear(); }

        ui.draw();
    }

    private void animationStep(){
        //setup and start the animation of nodes
        if(animationCount == (animationCountMax - 1)){
            for(Line line : graphLines.get(currentgraphindex + 1)){
                line.opacity = 0f;
            }
            ACyclicIterator<NodeGraphicsInfo> k;
            k = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
            locationMap = new HashMap<NodeGraphicsInfo,Coordinate>();
            tempMap = new HashMap<NodeGraphicsInfo,Coordinate>();
            while(k.hasNext()){
                NodeGraphicsInfo ngi = k.next();
                tempMap.put(ngi, new Coordinate(ngi.x,ngi.y,ngi.z));
                if(hasNode(ngi.node,dgraphs.get(currentgraphindex + 1))){
                    DisplayGraph dg = dgraphs.get(currentgraphindex + 1);
                    locationMap.put(ngi,getCoordinate(dg,ngi));
                    ngi.x += (locationMap.get(ngi).x - ngi.x) / (animationCount / 2);
                    ngi.y += (locationMap.get(ngi).y - ngi.y) / (animationCount / 2);
                    ngi.z += (locationMap.get(ngi).z - ngi.z) / (animationCount / 2);
                }else{
                    ngi.opacity -= OPACITY_COUNT;
                }
            }
            //Now check for nodes to be faded in
            nextGraphList = new ArrayList<NodeGraphicsInfo>();
            ACyclicIterator<NodeGraphicsInfo> acyc;
            acyc = new ACyclicIterator<NodeGraphicsInfo>(dgraphs.get(currentgraphindex + 1).preorderTraversal());
            while(acyc.hasNext()){
                NodeGraphicsInfo node = acyc.next();
                if(!hasNode(node.node,currentgraph))
                    node.opacity = 0;
                nextGraphList.add(node);
            }
        }          
        //continue node animation and fading out
        else if(animationCount < (animationCountMax - 1)){
            ACyclicIterator<NodeGraphicsInfo> k;
            k = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
            while(k.hasNext()){
                NodeGraphicsInfo ngi = k.next();
                if(hasNode(ngi.node,dgraphs.get(currentgraphindex + 1))){
                    (ngi).x += (locationMap.get(ngi).x - ngi.x) / (animationCount);
                    (ngi).y += (locationMap.get(ngi).y - ngi.y) / (animationCount);
                    (ngi).z += (locationMap.get(ngi).z - ngi.z) / (animationCount);
                }else{
                    ngi.opacity -= OPACITY_COUNT*1.3f;
                }
            }
            for(NodeGraphicsInfo ngi : nextGraphList)
            {
                if(!hasNode(ngi.node,currentgraph)){
                    ngi.opacity += OPACITY_COUNT;
                    drawNode(ngi);
                    for(Line line : graphLines.get(currentgraphindex)){
                        if(animationCount > animationCountMax / 2){
                            line.opacity -= OPACITY_COUNT/4;
                            line.draw(this);
                        }
                    }
                    for(Line line : graphLines.get(currentgraphindex + 1)){
                        line.opacity += OPACITY_COUNT/10;
                        line.draw(this);
                    }
                }
            }
        }
        animationCount--;
        //end of animation...reset animation count and reset coordinates
        if(animationCount <= 0){
            ACyclicIterator<NodeGraphicsInfo> k;
            k = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());
            while(k.hasNext()){
                NodeGraphicsInfo ngi = k.next();
                ngi.x = tempMap.get(ngi).x;
                ngi.y = tempMap.get(ngi).y;
                ngi.z = tempMap.get(ngi).z;
            }
            for(Line line : graphLines.get(currentgraphindex)){
                line.opacity = 255f;
            }
            currentgraph = dgraphs.get(currentgraphindex + 1);
            currentgraphindex++;
            animationCount = animationCountMax;
        }
    }

    private void drawNode(NodeGraphicsInfo n){
        if (n.gnt instanceof ObjectGraphRoot) { return; }
        pushMatrix(); pushStyle();
        GraphNodeType t = n.gnt;

        translate(n.x, n.y, n.z);

        fill(n.r, n.g, n.b, n.opacity);
        float strokeWeight = (n.opacity > 255f) ? 1 : (n.opacity / 255f);
        strokeWeight(strokeWeight);  
        box(n.width, 20f, 20f);

        float size = 0;
        String data = null;

        if ((rendermode & renderfrontback) != 0) {
            try{
              data = t.toString();
              size = textWidth(data);
              pushMatrix();
              translate(0f, 0f, 11f);
              fill(5,n.opacity);
              text(data, 0, 0f);

              translate(0f, 0f, -22f);

              rotateY(PI);
              //text(data, 0f, 0f);
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
    
    private Color getColor(Node n){
        if(n.gnt instanceof IntegerNode)
          return Color.lightGray;
        else if(n.gnt instanceof ObjectNode){
          return Color.cyan;
        }
        else if(n.gnt instanceof StackFrameNode)
          return Color.red;
        else if(n.gnt instanceof ArrayNode)
          return Color.orange;
        else if(n.gnt instanceof ObjectGraphRoot)
          return Color.magenta;
        return null;
    }
  private DisplayGraph displayGraph(Graph graph) {
    LinkedList<Node> list = new LinkedList<Node>();
    HashMap<Node, NodeGraphicsInfo> nodemap = new HashMap<Node, NodeGraphicsInfo>();
    HashSet<Node> seen = new HashSet<Node>();

    list.add(graph.getRoot());
    while (!list.isEmpty()) {
      Node node = list.pop();
      if (seen.contains(node)) {
        continue;
      }
      seen.add(node);

      if (!nodemap.containsKey(node)) {
        nodemap.put(node, new NodeGraphicsInfo(getColor(node), node));
      }
      NodeGraphicsInfo parent = nodemap.get(node);

      for (Node child : node.getChildren()) {//children
        list.add(child);

        if (!nodemap.containsKey(child)) {
          getColor(child);
          nodemap.put(child, new NodeGraphicsInfo(getColor(child), child));
        }
        parent.addChild(nodemap.get(child));
      }
    }

    return new DisplayGraph(nodemap.get(graph.getRoot()));
  }

    public NodeGraphicsInfo getParent(DisplayGraph dg, NodeGraphicsInfo ngi){
        ACyclicIterator<NodeGraphicsInfo> acyc = new ACyclicIterator<NodeGraphicsInfo>(dg.preorderTraversal());
        while(acyc.hasNext()){
            NodeGraphicsInfo parent = acyc.next();
            for(NodeGraphicsInfo child : parent.getChildren()){
                if(child.gnt.getUniqueID().equals(ngi.gnt.getUniqueID()))
                    return parent;
            }
        }
        return null;
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
        ArrayList<Line> lineList = new ArrayList<Line>();
        ACyclicIterator<NodeGraphicsInfo> i = new ACyclicIterator<NodeGraphicsInfo>(dg.preorderTraversal());
        while( i.hasNext()){
            NodeGraphicsInfo parent = i.next();
            for (NodeGraphicsInfo kid : parent.getChildren()) {
                if(parent.gnt instanceof ObjectGraphRoot){}else
                    lineList.add(new Line(parent.getCoordinate(), kid.getCoordinate()));
            }
        }
        graphLines.add(lineList);
    }
    
    
    private boolean hasNode(Node n, DisplayGraph g){
        ACyclicIterator<NodeGraphicsInfo> acyc = new ACyclicIterator<NodeGraphicsInfo>(g.preorderTraversal());
        while(acyc.hasNext()){
          if(acyc.next().node.gnt.getUniqueID().equals(n.gnt.getUniqueID()))
            return true;
        }
        return false;
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
    protected void showPrevGraph(){
      if(currentgraphindex <= 0) {
        System.err.println("No more to show you...");
        return;
      }else if(animationCount == animationCountMax){
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

    private Coordinate getCoordinate(DisplayGraph g, NodeGraphicsInfo thisNode){
      ACyclicIterator<NodeGraphicsInfo> k = new ACyclicIterator<NodeGraphicsInfo>(g.preorderTraversal());
      while(k.hasNext()){
          NodeGraphicsInfo ngi = k.next();
          if(thisNode.node.gnt.getUniqueID().equals(ngi.node.gnt.getUniqueID()))
              return new Coordinate(ngi.x,ngi.y,ngi.z);
      }
      return null;
    }
}