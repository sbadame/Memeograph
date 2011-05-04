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
    NodeGraphicsInfo from,to;
    public Line(NodeGraphicsInfo from, NodeGraphicsInfo to)
    {
        this.from = from;
        this.to = to;
    }
    public void draw(ProcessingApplet p)
    {
        p.pushStyle();
        p.strokeWeight(5);
        p.stroke(1f,Math.min(from.opacity, to.opacity));
        p.line(from.x, from.y, from.z, to.x, to.y, to.z);
        p.popStyle();
    }
    
    public boolean equals(Line line)
    {
        return(from.equals(line.from) && to.equals(line.to));
    }
}
