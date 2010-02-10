package memeograph.graphics;

import java.awt.*;
import java.awt.geom.*;
import memeograph.Tree;

/*
 * The class that graphically represents a Node in our tree
 */
public class Node {
    private static final int PADDING = 5;
    private static final int ROUNDING = 20;

    private static final Color BACKGROUND = Color.WHITE;
    private static final Color TEXT = Color.BLACK;
    private static final Color BORDER = Color.BLACK;
    private static final Stroke STROKE = new BasicStroke(2);
    
    //Taken from: http://www.3rd-evolution.de/tkrammer/docs/java_font_size.html
    //to ensure that we got the same font size regardless of OS/DPI/Resolution
    private static final int FONTSIZE = (int)Math.round(12.0 * Toolkit.getDefaultToolkit().getScreenResolution() / 72.0);
    private static final Font FONT = new Font("Arial", Font.BOLD, FONTSIZE);

    public static void drawLine(Node start, Node end, Graphics2D g){
        double startx = start.x;
        double start_dy = (start.getBounds(g).getHeight()/2);
        if (start.getBounds(g).getWidth() >  10 ){//Found expeirementally
            startx += (end.x - start.x) * (start_dy/(end.y-start.y));
        }
        g.setStroke(STROKE);
        g.setColor(BORDER);
        double starty = start.y + start_dy;

        double end_dy = (end.getBounds(g).getHeight()/2);
        double endx = end.x;
        double endy = end.y - end_dy;

        g.drawLine( (int)startx, (int)starty, (int)endx, (int)endy );
    }

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

    void draw(Graphics2D g)
    {
        g.setFont(FONT);

        Rectangle2D bounds = getBounds(g);
        width = bounds.getWidth();

        g.setStroke(STROKE);
        g.setColor(BACKGROUND);
        g.fillRoundRect((int)(x - (bounds.getWidth()/2)  - PADDING ),
                   (int)(y - (bounds.getHeight()/2) - PADDING ),
                   (int)bounds.getWidth()  + 2*PADDING,
                   (int)bounds.getHeight() + 2*PADDING,
                   ROUNDING, ROUNDING);

        g.setColor(BORDER);
        g.drawRoundRect((int)(x - (bounds.getWidth()/2)  - PADDING ),
                   (int)(y - (bounds.getHeight()/2) - PADDING ),
                   (int)bounds.getWidth()  + 2*PADDING,
                   (int)bounds.getHeight() + 2*PADDING,
                   ROUNDING, ROUNDING);

        g.setColor(TEXT);
        g.drawString(data.getTreeName(), (int)(x - bounds.getWidth()/2),
                                         (int)(y + bounds.getHeight()/2) );

        
    }

    public Rectangle2D getBounds(Graphics g){
        FontMetrics metrics = g.getFontMetrics(FONT);
        return metrics.getStringBounds(data.getTreeName(), g);
    }

    public void setOpacity(double opacity){
	this.opacity = opacity;

    }

    public double getOpacity(){
    	return opacity;
    }
}
