package memeograph.renderer.processing;

import java.util.Random;

/**
 * Store the information that we need to draw this node.
 */
public class NodeGraphicsInfo {
    public float x, y, z;
    public int r,g,b;

    public float opacity = 255;
    public float width = 70;

    public NodeGraphicsInfo(){
        Random rand = new Random();
        r = rand.nextInt(20) + 200;
        g = rand.nextInt(20) + 200;
        b = rand.nextInt(10) + 245;
    }
}
