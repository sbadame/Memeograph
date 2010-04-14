package memeograph.ui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.media.opengl.GLException;
import memeograph.DiGraph;
import memeograph.GraphBuilder;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

/**
 * Does the drawing and layout. Originally made by extending the moving eye example from processing.
 */
public class MemeoPApplet extends PApplet implements MouseWheelListener{
    static int PADDING = 20;
    static float K = 0.01f; //Spring constant (Along the Y)
    static float M = 0.95f; //Magnet contents (Along the X)
    static float FRICTION = .95f;
    static int MOVE_TICK = 50;

    private Map<DiGraph, Node> positions = new HashMap<DiGraph, Node>();
    private Grid rails  = new Grid();

    GraphBuilder builder;
    boolean isReady = false;
    int elipseCount = 1;

    private boolean laidout = false;

    private int wanted_width;
    private int wanted_height;

    PFont font;

    //Camera Info
    PVector pos;
    PVector dir;
    PVector camNorth = new PVector(0,1,0);

    //Text Rendering info
    private final int renderfrontback = 1;
    private final int rendertopbottom = 2;

    private int rendermode = renderfrontback;

    public MemeoPApplet(GraphBuilder grapher, int width, int height){
        this.builder = grapher;
        this.wanted_height = height;
        this.wanted_width = width;
        addMouseWheelListener(this);
    }


    @Override
    public void setup(){
        //Full screen, go big or go home!
        try{
            size(wanted_width, wanted_height, P3D);
            //size(wanted_width, wanted_height, OPENGL);
        }catch(GLException gle){
            gle.printStackTrace();
            System.exit(1);
        }
        background(102);

        font = createFont("SansSerif.bold", 18);
        textFont(font);
        textAlign(CENTER, CENTER);

        //Lets see if we can slow down the stacks rendering to 30fps
        frameRate(25);
        pos = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*60.0f / 360.0f));
        dir = new PVector(width/2.0f, height/2.0f, 0);

        camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, camNorth.x, camNorth.y, camNorth.z);
        smooth();

        //Build the graph in another thread
        new Thread(){
            @Override
            public void run(){
                builder.addEventRequests();
                DiGraph.listener = MemeoPApplet.this;
                while(true){
                    builder.step();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        System.err.println("Can't sleep between steps");
                    }
                }
            }
        }.start();
    }


    @Override
    public void draw(){
        background(102);
        pushStyle();
        pushMatrix();
        if (builder.isBuilt()) {
            camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, 0, 1, 0);

            //First check if we have to layout this stuff out
            if (!laidout) {
                layout(builder.getStacks());
            }
            //jiggle our layout
            adjust();

            //Now draw the lines between the nodes
            for (Node n : positions.values()) {
                for (DiGraph kid : n.data.getChildren()) {
                    Node knode = positions.get(kid);
                    if (n != null && knode != null)
                        drawLine(n, knode);
                }
            }

            //Draw the nodes ontop of the lines. Awesome.
            boolean x = true;
            for (Node n : positions.values()) {
                if (x) {
                    x = false;
                    //System.out.println(n.data.getTreeName());
                }
                drawNode(n);
            }
        }else{
            elipseCount = (++elipseCount)%4;
            StringBuilder loadingtxt = new StringBuilder("Building Graph");
            for (int i = 0; i < elipseCount; i++) {
                loadingtxt.append(".");
            }

            textMode(SCREEN);
            textAlign(LEFT);
            text(loadingtxt.toString(), width/2, height/2);
        }

        popMatrix();
        popStyle();
    }

    private void drawLine(Node from, Node to){
        strokeWeight(5);
        line((float)from.x, (float)from.y, (float)from.z,
                (float)to.x, (float)to.y, (float)to.z);
    }

    private void drawNode(Node n){
        pushMatrix();
        translate((float)n.x, (float)n.y, (float)n.z);

        fill(n.r, n.g, n.b);
        strokeWeight(1);
        box((float)n.width, 20f, 20f);

        float size = 0;
        String data = null;

        if ((rendermode & renderfrontback) != 0) {
            data = n.data.getTreeName();
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
            if (data == null){data = n.data.getTreeName(); size = textWidth(data);}

            translate(0f, 11f, 0f);
            fill(5);
            rotateX(-PI/2);
            text(n.data.getTreeName(), -size/2, 0f);
            rotateX(PI/2);

            translate(0f, -22f, 0f);

            rotateX(-PI/2);
            rotateY(PI);
            textAlign(LEFT);
            text(n.data.getTreeName(), -size/2, 0f);
        }
        popMatrix();
    }

    private void layout(Collection<DiGraph> t)
    {
        rails.clear();
        positions.clear();
        
        for (DiGraph stack : t) {
            layout(stack, -10, 0);
            
            DiGraph sf = stack;
            int y = 0;
            Set<DiGraph> seen = new HashSet<DiGraph>();
            while (sf.getSoftwareChildren().size() >= 1){
                sf = sf.getSoftwareChildren().firstElement();
                if (seen.contains(sf)) break;
                
                y+=50;
                layout(sf, -10, y);
                seen.add(sf);
            }

            for (Vector<Node> rail : rails) {
                double x = 0;
                for (Node n : rail) {
                    n.x = x;
                    x += n.width + 50;
                }
            }
        }

        laidout = true; 
    }

    private void layout(DiGraph t, int z, int y)
    {
        if (positions.get(t) != null) return;
        Node n = new Node(t, 0, y*50, z*50);
        n.width = textWidth(t.getTreeName());

        positions.put(t, n);
        rails.add(z, y, n);

        for (DiGraph kid : t.getDataChildren()) {
            layout(kid, z-1, y);
        }

        for (DiGraph kid : t.getSoftwareChildren()) {
            layout(kid, z, y+1);
        }
    }

    private double adjust(){
        if (!laidout) return -1;

        double total = 0;

        for (Node n : positions.values()) {
            n.fx = 0;
        }

        //magnets
        
        for (Vector<Node> layer : rails) {
            for(int j = 0; j < layer.size(); j++){
                if (j > 0){
                    Node r = layer.get(j);
                    Node l = layer.get(j-1);
                    double d = (l.x + l.width/2) - (r.x - r.width/2);
                    layer.get(j).fx += 1000.0 / (d*d + 1);
                }

                if (j < layer.size() - 1){
                    Node l = layer.get(j);
                    Node r = layer.get(j+1);
                    double d = l.x + l.width/2 - (r.x - r.width/2);
                    layer.get(j).fx -= 1000.0 / (d*d + 1);
                }
            }

        }

        //springs
        for (Node n : positions.values()) {
            for (DiGraph kidt : n.data.getChildren()) {
                Node kid = positions.get(kidt);
                if (kid == null) continue;

                // F = -k*d
                double dx = n.x - kid.x;
                double dy = n.y - kid.y;
                double dz = n.z - kid.z;

                double d = Math.sqrt(dx*dx + dy*dy + dz * dz);
                double F = K * d;
                kid.fx += F*dx/d;
            }
        }

        
        for (Vector<Node> layer : rails) {
            for(int j = 0; j < layer.size(); j++){
                Node n = layer.get(j);
                n.vx = n.vx*FRICTION + 1*n.fx;
                double newx = n.x + 0.1*n.vx;
                total+= Math.abs(n.fx);
                n.x = newx;
            }

            // Not too close, okay...
            for(int j = 1; j < layer.size(); j++){
                Node l = layer.get(j-1);
                Node n = layer.get(j);
                if (l.x + l.width/2 + n.width/2 + PADDING > n.x) {
                    n.x = l.x + l.width/2 + n.width/2 + PADDING;
                }
            }
        }
        

        //System.out.println(total);
        return total;
    }

//  @Override
//  public void mouseClicked()
//  {
//      float f =dist(mouseX, mouseY, width-50, height-50);
//      float e =dist(mouseX, mouseY, width-100, height-50);
//      if (f < 50) {
//          System.out.println("Step");
//      }else if (e < 50){
//          System.out.println("in");
//      }//otherwise do nothing
//  }

    float dtheta = .03f;
    
    @Override
    public void mouseDragged()
    {
        float dy = pmouseY - mouseY;
        if (dy != 0) {
            float y = (pos.y-dir.y);
            float z = (pos.z-dir.z);
            float r = sqrt(y*y + z*z);
            float theta = atan2(y, z);

            float theta_new = theta + ((dy > 0) ? dtheta : (-1*dtheta));
            y = sin(theta_new) * r;
            z = cos(theta_new) * r;
            pos.y = dir.y + y;
            pos.z = dir.z + z;
        }

        float dx = pmouseX - mouseX;
        if (dx != 0) {
            float x = (pos.x-dir.x);
            float z = (pos.z-dir.z);
            float r = sqrt(x*x + z*z);
            float theta = atan2(z, x);

            float theta_new = theta + ((dx < 0) ? dtheta : (-1*dtheta));
            x = cos(theta_new) * r;
            z = sin(theta_new) * r;
            pos.x = dir.x + x;
            pos.z = dir.z + z;
        }
    }

    @Override
    public void keyPressed(){
        char k = (char)key;
        switch(k){
            case 'w':
            case 'W': translateCameraY(-MOVE_TICK); break;
            case 's':
            case 'S': translateCameraY(MOVE_TICK); break;
            case 'a':
            case 'A': translateCameraX(-MOVE_TICK); break;
            case 'd':
            case 'D': translateCameraX(MOVE_TICK); break;
            case 't':
            case 'T': toggleRenderMode(); break;
            default: break;
        }
    }

    private void translateCameraY(float amount){
        PVector camera = PVector.sub(dir,pos);
        PVector cross = camera.cross(camNorth);
        PVector up = cross.cross(camera);
        up.normalize();
        up.mult(amount);
        pos.add(up);
        dir.add(up);
    }

    private void translateCameraX(float amount){
        PVector camera = PVector.sub(dir,pos);
        PVector cross = camera.cross(camNorth);
        cross.normalize();
        cross.mult(amount);
        pos.add(cross);
        dir.add(cross);
    }

    private void toggleRenderMode(){
        rendermode = (rendermode + 1) % ((renderfrontback|rendertopbottom) + 1);
    }

    @Override
    public void mouseMoved(){
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = -1*e.getWheelRotation(); //notches goes negative if the
                                            //wheel is scrolled up.
             
        PVector camera = PVector.sub(dir,pos);
        camera.normalize();
        
        pos.add(PVector.mult(camera, (float)notches * 100f));
        dir.add(PVector.mult(camera, (float)notches * 100f));
    }

    public void change(){
        laidout = false;
    }
}
