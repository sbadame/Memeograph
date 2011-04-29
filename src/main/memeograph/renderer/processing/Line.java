/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package memeograph.renderer.processing;

/**
 *
 * @author mwaldron74
 */
public class Line {
    Coordinate to,from;
    float opacity;
    public Line(Coordinate from, Coordinate to)
    {
        this(from, to, 255);
    }
    
    public Line(Coordinate from, Coordinate to, float opacity)
    {
        this.to = to;
        this.from = from;
        this.opacity = opacity;
    }
    
    public void changeTo(Coordinate c)
    {
        to = c;
    }
    
    public void opacity(float op)
    {
        opacity = op;
    }
    
    public void draw(ProcessingApplet p)
    {
        p.pushStyle();
        p.strokeWeight(5);
        p.stroke(1f,opacity);
        p.line(from.x, from.y, from.z, to.x, to.y, to.z);
        p.popStyle();
    }
}
