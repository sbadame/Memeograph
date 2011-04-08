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
    private static final float OPACITY_COUNT = ((255.0f)/(animationCountMax/2));
    private int animationCount = animationCountMax;
    private int currentgraphindex = 0;
    private HashMap<NodeGraphicsInfo,Float> locationXMap;
    private HashMap<NodeGraphicsInfo,Float> locationYMap;
    private HashMap<NodeGraphicsInfo,Float> locationZMap;
    private ArrayList<NodeGraphicsInfo> nextGraphList;

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
    
        animationStep();
        
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

    private void animationStep(){
      if(currentgraphindex < dgraphs.size() -1 && animationCount != animationCountMax){
          ACyclicIterator<NodeGraphicsInfo> k;
          k = new ACyclicIterator<NodeGraphicsInfo>(currentgraph.preorderTraversal());

          //setup and start the animation of nodes
          if(animationCount == (animationCountMax - 1)){
              locationXMap = new HashMap<NodeGraphicsInfo,Float>();
              locationYMap = new HashMap<NodeGraphicsInfo,Float>();
              locationZMap = new HashMap<NodeGraphicsInfo,Float>();
              while(k.hasNext()){
                  NodeGraphicsInfo ngi = k.next();
                  if(hasNode(ngi.node,dgraphs.get(currentgraphindex + 1))){
                      DisplayGraph dg = dgraphs.get(currentgraphindex + 1);
                      locationXMap.put(ngi,getX(dg,ngi));
                      locationYMap.put(ngi,getY(dg,ngi));
                      locationZMap.put(ngi,getZ(dg,ngi));
                      ngi.x += (locationXMap.get(ngi) - getX(currentgraph,ngi)) / (animationCount / 2);
                      ngi.y += (locationYMap.get(ngi) - getY(currentgraph,ngi)) / (animationCount / 2);
                      ngi.z += (locationZMap.get(ngi) - getZ(currentgraph,ngi)) / (animationCount / 2);
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
              while(k.hasNext()){
                  NodeGraphicsInfo ngi = k.next();
                  if(hasNode(ngi.node,dgraphs.get(currentgraphindex + 1))){
                      ngi.x += (locationXMap.get(ngi) - getX(currentgraph,ngi)) / (animationCount);
                      ngi.y += (locationYMap.get(ngi) - getY(currentgraph,ngi)) / (animationCount);
                      ngi.z += (locationZMap.get(ngi) - getZ(currentgraph,ngi)) / (animationCount);
                  }else{
                      ngi.opacity -= OPACITY_COUNT;
                  }
              }
              for(NodeGraphicsInfo ngi : nextGraphList)
              {
                  if(!hasNode(ngi.node,currentgraph)){
                      ngi.opacity += OPACITY_COUNT;
                      drawNode(ngi);
                  }
              }
          }
          animationCount--;

        //end of animation...reset animation count
        if(animationCount == 0){
           currentgraph = dgraphs.get(currentgraphindex + 1);
           currentgraphindex++;
           animationCount = animationCountMax;
        }
    }
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
    private Color getColor(Node n){
        if(n.gnt instanceof IntegerNode)
          return Color.lightGray;
        else if(n.gnt instanceof ObjectNode){
          if(n.gnt.getName().equals("Leaf()"))
            return Color.green;
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

    private Float getX(DisplayGraph g, NodeGraphicsInfo thisNode){
      ACyclicIterator<NodeGraphicsInfo> k = new ACyclicIterator<NodeGraphicsInfo>(g.preorderTraversal());
      while(k.hasNext()){
          NodeGraphicsInfo ngi = k.next();
          if(thisNode.node.gnt.getUniqueID().equals(ngi.node.gnt.getUniqueID()))
              return ngi.x;
      }
      return -1f;
    }
    
    private Float getY(DisplayGraph g, NodeGraphicsInfo thisNode){
      ACyclicIterator<NodeGraphicsInfo> k = new ACyclicIterator<NodeGraphicsInfo>(g.preorderTraversal());
      while(k.hasNext()){
          NodeGraphicsInfo ngi = k.next();
          if(thisNode.node.gnt.getUniqueID().equals(ngi.node.gnt.getUniqueID()))
              return ngi.y;
      }
      return -1f;
    }
    
    private Float getZ(DisplayGraph g, NodeGraphicsInfo thisNode){
      ACyclicIterator<NodeGraphicsInfo> k = new ACyclicIterator<NodeGraphicsInfo>(g.preorderTraversal());
      while(k.hasNext()){
          NodeGraphicsInfo ngi = k.next();
          if(thisNode.node.gnt.getUniqueID().equals(ngi.node.gnt.getUniqueID()))
              return ngi.z;
      }
      return -1f;
    }
    
}