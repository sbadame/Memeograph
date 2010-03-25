package memeograph.ui;

import java.awt.Color;
import java.util.Random;
import memeograph.Tree;

/*
 * The class that graphically represents a Node in our tree
 */
public class Node {
    public double x, y, z;
    public int r,g,b;

    public double fx;
    public double vx=0;
    public double opacity = 1;
    public double width = 70;
    public Tree data;
    
    public Node(Tree data, double x, double y)
    {
        this(data, x, y, 0.0);
    }

    public Node(Tree data, double x, double y, double z)
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


    public void setOpacity(double opacity){
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
