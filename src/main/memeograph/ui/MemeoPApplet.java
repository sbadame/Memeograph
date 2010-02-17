package memeograph.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import memeograph.Tree;
import memeograph.TreeChangeListener;
import memeograph.graphics.Node;
import processing.core.PApplet;
import processing.core.PFont;

/**
 * Here is the moving eye example from processing
 */
public class MemeoPApplet extends PApplet implements TreeChangeListener{
    static final int PADDING = 20;

    private Map<Tree, Node> positions;
    private Vector<Vector<Node>> layers;

    private Tree tree;
    private boolean treechanged = false;
    private boolean laidout = false;

    PFont font;

    public MemeoPApplet(Tree tree){
        this.tree = tree;
    }

    float camz;

    @Override
    public void setup(){
        size(1024, 768, P3D);
        background(102);

        font = createFont("SansSerif.plain", 12);
        textFont(font);
        textAlign(CENTER, CENTER);

        //Lets see if we can slow down the tree rendering to 30fps
        frameRate(30);

        camz = (height/2.0f) / tan(PI*60.0f / 360.0f);
        camera(width/3.0f, height/3.0f, camz,
                width/2.0f, height/2.0f, 0, 
                0, 1, 0);
    }


    @Override
    public void draw(){
        background(102);

        //First check if we have to layout this stuff out
        if (!laidout) {
            treechanged = false;
            layout(tree, width/2, PADDING);
        }

        //jiggle our layout
        adjust();

        //Now draw the lines between the nodes
        for (Node n : positions.values()) {
            for (Tree kid : n.data.getChildren()) {
                Node knode = positions.get(kid);
                drawLine(n, knode);
            }
        }

        //Draw the nodes ontop of the lines. Awesome.
        for (Node n : positions.values()) {
            drawNode(n);
        }
    }

    private void drawLine(Node from, Node to){
        line((float)from.x, (float)from.y, (float)to.x, (float)to.y);
    }

    private void drawNode(Node n){
        text(n.data.getTreeName(), (float)n.x, (float)n.y);
    }

    private void layout(Tree t, double x, double y){
        positions = new HashMap<Tree,Node>();
        layers = new Vector<Vector<Node>>();
        layers.add(new Vector<Node>());

        Queue<Tree> curr_layer = new LinkedList<Tree>();
        curr_layer.add(t);

        int layer = 0;
        Queue<Tree> next_layer = new LinkedList<Tree>();

        double xpos = x;

        while (!curr_layer.isEmpty() || !next_layer.isEmpty()) {
            if (curr_layer.isEmpty()) {
                layer += 1;
                curr_layer = next_layer;
                next_layer = new LinkedList<Tree>();
                layers.add(new Vector<Node>());
                xpos = x;
            }

            t = curr_layer.remove();
            t.addTreeChangeListener(this);

            Node n = new Node(t, xpos, (layer+1)*50);
            n.width = textWidth(n.data.getTreeName());
            xpos += n.width + 100;

            layers.get(layer).add(n);
            positions.put(t, n);

            for (Tree kid : t.getChildren()) {
                next_layer.add(kid);
            }
        }

        laidout = true; 
    }

    private double adjust(){
        if (!laidout) return -1;

        double total = 0;

        for (Node n : positions.values()) {
            n.fx = 0;
        }

        //magnets
        for (int i = 1; i < layers.size(); i++) {
            Vector<Node> layer = layers.get(i);

            for(int j = 0; j < layer.size(); j++){
                if (j > 0){
                    Node r = layer.get(j);
                    Node l = layer.get(j-1);
                    double d = (l.x + l.width/2) - (r.x - r.width/2);
                    layer.get(j).fx += 100.0 / (d*d + 1);
                }

                if (j < layer.size() - 1){
                    Node l = layer.get(j);
                    Node r = layer.get(j+1);
                    double d = l.x + l.width/2 - (r.x - r.width/2);
                    layer.get(j).fx -= 100.0 / (d*d + 1);
                }
            }
        }

        //springs
        for (Node n : positions.values()) {
            for (Tree kidt : n.data.getChildren()) {
                Node kid = positions.get(kidt);

                // F = -k*d
                double dx = n.x - kid.x;
                double dy = n.y - kid.y;

                double d = Math.sqrt(dx*dx + dy*dy);
                double F = 0.01 * d;
                kid.fx += F*dx/d;
            }
        }

        for (int i = 1; i < layers.size(); i++) {
            Vector<Node> layer = layers.get(i);
            for(int j = 0; j < layer.size(); j++){
                Node n = layer.get(j);
                n.vx = n.vx*0.90 + 1*n.fx;
                double newx = n.x + 0.1*n.vx;
                total+= Math.abs(100*n.fx);
                n.x = newx;
            }

            for(int j = 1; j < layer.size(); j++){
                Node l = layer.get(j-1);
                Node n = layer.get(j);
                if (l.x + l.width/2 + n.width/2 + 1 > n.x) {
                    System.out.println(l.x + " " + n.x + " " + (l.width/2) + " " + (n.width/2));
                    n.x = l.x + l.width/2 + n.width/2 + 1;
                }
            }
        }

        return total;
    }

    public void kidAdded(Tree parent, Tree addedNode) {
        throw new UnsupportedOperationException();
    }

    public void childrenRemoved(Tree parent) {
        throw new UnsupportedOperationException();
    }

    public void dataChanged(Tree parent) {
        throw new UnsupportedOperationException();
    }


    public void mouseDragged()
    {
        int d = mouseY - pmouseY;

        if (d > 0) {
            camz *= 1.1f;
        } else if (d < 0) {
            camz /= 1.1f;
        }

        camera(width/3.0f, height/3.0f, camz,
                width/2.0f, height/2.0f, 0, 
                0, 1, 0);
    }
}
