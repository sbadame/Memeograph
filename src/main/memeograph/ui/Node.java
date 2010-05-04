package memeograph.ui;

import java.awt.Color;
import java.util.Random;
import memeograph.DiGraph;

/*
 * The class that graphically represents a Node in our tree
 */
public class Node {
    public float x, y, z;
    public int r,g,b;

    public float fx;
    public float vx=0;
    public float opacity = 1;
    public double width = 70;
    public DiGraph data;
    
    public Node(DiGraph data, float x, float y)
    {
        this(data, x, y, 0f);
    }

    public Node(DiGraph data, float x, float y, float z)
    {
        this.data = data;
        this.x = x;
        this.y = y;
        this.z = z;

        if (data.getColor() == null) {
            Random rand = new Random();
            r = rand.nextInt(20) + 200;
            g = rand.nextInt(20) + 200;
            b = rand.nextInt(10) + 245;
        }else{
            Color c = data.getColor();
            r = c.getRed();
            g = c.getGreen();
            b = c.getBlue();
        }
    }


    public void setOpacity(float opacity){
	this.opacity = opacity;

    }

    public double getOpacity(){
    	return opacity;
    }

    public void setColor(int r, int g, int b){
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
