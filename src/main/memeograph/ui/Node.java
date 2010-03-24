package memeograph.ui;

import java.awt.*;
import java.awt.geom.*;
import memeograph.Tree;

/*
 * The class that graphically represents a Node in our tree
 */
public class Node {
    private static final int PADDING = 5;

    public double x, y;
    public double fx;
    public double vx=0;
    public double opacity = 1;
    public double width = 70;
    public Tree data;
    
    public Node(Tree data, double x, double y)
    {
        this.data = data;
        this.x = x;
        this.y = y;
    }


    public void setOpacity(double opacity){
	this.opacity = opacity;

    }

    public double getOpacity(){
    	return opacity;
    }
}
