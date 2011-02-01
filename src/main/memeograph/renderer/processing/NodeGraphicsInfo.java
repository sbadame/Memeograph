package memeograph.renderer.processing;

import java.awt.Color;
import java.util.Random;

/**
 * Store the information that we need to draw this node.
 */
public class NodeGraphicsInfo {
    public float x, y, z;
    public int r,g,b;

    public float opacity = 255;
    public float width = 70;

    public NodeGraphicsInfo(Color c){
        Random rand = new Random();
        if (null == c) {
          r = 200; g = 200; b = 245;
        } else {
          r = c.getRed(); g = c.getGreen(); b = c.getBlue();
        }

        r = rand.nextInt(20) + (r - 10);
        g = rand.nextInt(20) + (g - 10);
        b = rand.nextInt(10) + (b - 5);

        r = r < 255 ? (r > 0 ? r : 0) : 255;
        g = g < 255 ? (g > 0 ? g : 0) : 255;
        b = b < 255 ? (b > 0 ? b : 0) : 255;
    }
}
