package memeograph.graphics;

import java.awt.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import memeograph.*;

/**
 * The Coponent that actually does the dirty work of drawing the tree
 */
public class TreeDisplay extends JComponent implements TreeChangeListener{

    private static final Color BACKGROUND = Color.decode("0xAAffff");
    private static final int PADDING = 20;

    private Map<Tree, Node> positions;
    private Vector<Vector<Node>> layers;

    //This is probably over kill, Dr. Boothe gets to decide.
    private BlockingQueue<Animation> animations = new LinkedBlockingQueue<Animation>();

    private Tree tree;
    private String message = "Enter some code to run!";
    private boolean treechanged = false;
    private boolean laidout = false;

    public TreeDisplay()
    {
        super();
        start();
    }

    public void setTree(Tree tree)
    {
        this.tree = tree;
        treechanged = true;
        message = null;
        laidout = false;
    }

    public void setMessage(String message) 
    {
        this.message = message;
    }

    public void start()
    {
        new Thread() {
            public void run()
            {
                while (true) {
                    adjust();
                    repaint();
                    try { Thread.sleep(1); } catch (InterruptedException ie) {}
                }
            }
        }.start();

        new Thread() {
            public void run()
            {
                while (true) {
                    try {
                        Animation a = animations.take();
                        a.animate();
                    } catch (InterruptedException ie) {}
                }
            }
        }.start();
    }

    private void layout(Tree t, double x, double y) 
    {
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
            xpos += n.width + PADDING;

            layers.get(layer).add(n);
            positions.put(t, n);

            for (Tree kid : t.getChildren()) {
                next_layer.add(kid);
            }
        }

        laidout = true;
    }

    @Override
    public void paint(Graphics g)
    {
        synchronized (this) {
            Graphics2D g2 = graphicsPrep(g);
            if (message != null){
                g.drawString(message, 0, getHeight()/2);
                return;
            } 

            if (treechanged) {
                treechanged = false;
                layout(tree, 0, PADDING);
            }

            for (Node n : positions.values()) {
                for (Tree kid : n.data.getChildren()) {
                    Node knode = positions.get(kid);
                    Node.drawLine(n, knode, g2);
                }
            }

            for (Node n : positions.values()) {
                n.draw(g2);
            }


            g2.dispose();
        }
    }

    public double adjust()
    {
        synchronized (this) {
            if (!laidout) return -1;

            double total = 0;

            for (Node n : positions.values())
                n.fx = 0;

            // Magnets
            for (int i = 1; i < layers.size(); i++) {
                Vector<Node> layer = layers.get(i);

                for (int j = 0; j < layer.size(); j++) {
                    if (j > 0) {
                        Node r = layer.get(j);
                        Node l = layer.get(j-1);
                        double d = (l.x + l.width/2) - (r.x - r.width/2);
                        layer.get(j).fx += 1000.0 / (d*d);
                    }

                    if (j < layer.size()-1) {
                        Node l = layer.get(j);
                        Node r = layer.get(j+1);
                        double d = (l.x + l.width/2) - (r.x - r.width/2);
                        layer.get(j).fx -= 1000.0 / (d*d);
                    }
                }
            }

            // Springs
            for (Node n : positions.values()) {
                for (Tree kidt : n.data.getChildren()) {
                    Node kid = positions.get(kidt);

                    // F = -k*d
                    double dx = n.x - kid.x;
                    double dy = n.y - kid.y;

                    double d = Math.sqrt(dx * dx + dy * dy);
                    double F = .01 * d;
                    kid.fx += F * dx / d;
                }
            }

            for (Node n : positions.values()) {
                n.vx = n.vx*.9 + 100*n.fx;
                n.x += .01*n.vx;
                total += Math.abs(100*n.fx);
            }

            return total;
        }
    }

    private Graphics2D graphicsPrep(Graphics g1)
    {
        Graphics2D g = (Graphics2D)g1;
        int depth = tree != null ? tree.depth() : 1;

        double totalpixels = depth * 50 + 40;
        double available = getHeight();
        double scale = available / totalpixels;

        g.scale(scale, scale);
        g.translate(getWidth()/2/scale,0);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

    /**
     * ADD YOUR ANIMATION ADDING TO QUEUE CODE HERE!!!
     */
    public void kidAdded(final Tree parent, final Tree addedNode){
        animations.offer(new Animation() {
                    void animate()
                    {
                        System.out.println("Animating the addition (not) of " + addedNode + " to " + parent);
                        try { Thread.sleep(2000); } catch (Exception e) {}
                        System.out.println("Done animating the addition (not) of " + addedNode + " to " + parent);
                    }
                });
    }

    public void childrenRemoved(final Tree parent){
        animations.offer(new Animation() {
                    void animate()
                    {
                        System.out.println("Animating the removal (not) of " + parent);
                        try { Thread.sleep(2000); } catch (Exception e) {}
                        System.out.println("Done animating the removal (not) of " + parent);
                    }
                });
    }

    public void dataChanged(final Tree parent){
        animations.offer(new Animation() {
                    void animate()
                    {
                        System.out.println("Animating the removal (not) of " + parent);
                        try { Thread.sleep(2000); } catch (Exception e) {}
                        System.out.println("Done animating the removal (not) of " + parent);
                    }
                });
    }
}
