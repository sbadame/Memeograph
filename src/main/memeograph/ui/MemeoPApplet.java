package memeograph.ui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.media.opengl.GLException;
import memeograph.DiGraph;
import memeograph.GraphBuilder;
import memeograph.StackObject;
import memeograph.SuperHeader;
import memeograph.ThreadHeader;
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

       DiGraph.listener = MemeoPApplet.this;
    }


    @Override
    public void draw(){
        background(102);
        camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, 0, 1, 0);

        //First check if we have to layout this stuff out
        if (!laidout) {
            HashMap<DiGraph, Node> hashMap = new HashMap<DiGraph, Node>();
            Grid newrails = new Grid();
            layout(builder.getSuperNode(), hashMap, newrails);
            positions = hashMap;
            rails = newrails;
        }

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
                //System.out.println(n.data.name());
            }
            drawNode(n);
        }


        //Draw the UI
        //Play button
        pushStyle();
        camera(); //Reset the view port and do the 2d drawing
        ellipseMode(CENTER);
        fill(0,255,0);
        ellipse(50, 50, 50, 50);
        fill(0,0,255);
        ellipse(120, 50, 50, 50);

        //Make the Text
        fill(0);
        textSize(35);
        textAlign(CENTER);
        text(stepText, 50, 65);
        text(playText, 120, 64);
        popStyle();
    }

    String stepText = "S";
    String playText = ">";
    Thread stepThread;
    boolean playing = false;

    @Override
    public void mouseClicked()
    {
        float f =dist(mouseX, mouseY, 50, 50);
        float e =dist(mouseX, mouseY, 120, 50);
        if (f < 50) {
            if (stepThread == null) {
                stepText = "";
                stepThread = new Thread(){
                    @Override
                    public void run(){
                        builder.step();
                        laidout = false;
                        stepThread = null;
                        stepText = "S";
                    }
                };
                stepThread.start();
            }
        }else if (e < 50){
            if (playing) {
                playing = false;
                stepThread.interrupt();
                stepThread = null;
                playText = ">";
            }else{
                if (stepThread == null) {
                    playText = "||";
                    playing = true;
                    stepThread = new Thread(){
                        @Override
                        public void run(){
                            while(playing){
                                builder.step();
                                laidout = false;
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException ex) {
                                    //I guess someone hit stop!
                                    break;
                                }
                            }
                            stepThread = null;
                        }
                    };
                    stepThread.start();
                }else{
                    //If it's not playing, but is also not not null that means
                    //That the step button was pressed and is currently running
                    //Therefore, we shouldn't do anything
                }
            }
        }
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
            data = n.data.name();
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
            if (data == null){data = n.data.name(); size = textWidth(data);}

            translate(0f, 11f, 0f);
            fill(5);
            rotateX(-PI/2);
            text(n.data.name(), -size/2, 0f);
            rotateX(PI/2);

            translate(0f, -22f, 0f);

            rotateX(-PI/2);
            rotateY(PI);
            textAlign(LEFT);
            text(n.data.name(), -size/2, 0f);
        }
        popMatrix();
    }

    private void layout(SuperHeader digraph, Map<DiGraph, Node> map, Grid rls)
    {
        for (DiGraph d : digraph.getThreads()) {
            ThreadHeader thread = (ThreadHeader)d;
            layout(thread, -10, 0, map, rls);
            if (thread.hasFrame() == false) {
                continue;
            }

            StackObject sf = thread.getFrame();
            int y = 0;
            Set<DiGraph> seen = new HashSet<DiGraph>();
            while (sf != null && sf.hasNextFrame()){
                sf = sf.nextFrame();
                if (seen.contains(sf)) break;
                y+=50;
                layout(sf, -10, y, map, rls);
                seen.add(sf);
            }

            for (Vector<Node> rail : rls) {
                double x = 0;
                for (Node n : rail) {
                    n.x = x;
                    x += n.width + 50;
                }
            }
        }

        laidout = true;
    }

    private void layout(DiGraph t, int z, int y, Map<DiGraph, Node> map, Grid rls)
    {
        if (map.get(t) != null) return;
        Node n = new Node(t, 0, y*50, z*50);
        n.width = textWidth(t.name());

        map.put(t, n);
        rls.add(z, y, n);

        for (DiGraph kid : t.getZChildren()) {
            layout(kid, z-1, y, map, rls);
        }

        for (DiGraph kid : t.getYChildren()) {
            layout(kid, z, y+1, map, rls);
        }

    }

    private float dtheta = .03f;
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

}
