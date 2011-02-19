package memeograph.renderer.processing.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import memeograph.renderer.processing.ProcessingApplet;
import processing.core.PApplet;

public class UI {

    public static final String FPS = "FPS";
    public static final String GRAPH_COUNTER = "GRAPH_COUNTER";
    public static final String LOADING = "LOADING";
    
    public final ProcessingApplet p;
    private final int PADDING = 5;

    private WidgetContainer topleft = new LeftJustifiedTopDown(){{
        add(new TextWidget(FPS){
            @Override
            public String getText() { return "FPS: " + ProcessingApplet.round(p.frameRate); }
        });

        newRow();

        add(new TextWidget(GRAPH_COUNTER){
            @Override
            public String getText() { 
              return "Graph " + (p.getGraphs().indexOf(p.getCurrentGraph()) + 1) + " of " + p.getGraphs().size();
            }
        });
    }};

    private WidgetContainer bottomright = new RightJustifiedBottomUp(){{
        add(new TextWidget("Controls"));
        newRow();
        add(new TextWidget("N : next graph"));
        newRow();
        add(new TextWidget("T : toggle text"));
        newRow();
        add(new TextWidget("Mouse Wheel: zoom in/out"));
    }};

    private WidgetContainer bottomleft = null;

    private WidgetContainer center = new LeftJustifiedTopDown(){{
        add( new TextWidget(LOADING){
            @Override
            public String getText(){
               return "Loading." + repeat((p.millis() / 500) % 4);
            }

            public String repeat(int times){
               if (times <= 0) return "";
               else return "." + repeat(times-1);
            }
        });
    };
  };

    public UI(ProcessingApplet pApplet){
        this.p = pApplet;
    }

    public void init(){
        topleft = getTopLeft();
        bottomright = getBottomRight();
        bottomleft = getBottomLeft();
    }

    public void draw(){
        p.fill(Color.BLACK.getRGB());
        p.hint(PApplet.DISABLE_DEPTH_TEST);
        p.camera();
        p.textAlign(PApplet.LEFT, PApplet.TOP);

        if (topleft != null) {
            p.pushMatrix();
                p.translate(PADDING,PADDING);
                topleft.draw(p);
            p.popMatrix();
        }

        if (bottomright != null) {
            p.pushMatrix();
                p.translate(p.width-PADDING, p.height-PADDING);
                bottomright.draw(p);
            p.popMatrix();
        }

        if (bottomleft != null) {
            p.pushMatrix();
                p.translate(0, p.height-PADDING);
                bottomleft.draw(p);
            p.popMatrix();
        }

        if ( center != null) {
            p.pushMatrix();
                Dimension size = center.getSize(p);
                p.translate((p.width - size.width)/2, (p.height - size.height)/2);
                center.draw(p);
            p.popMatrix();
        }
    }

    public WidgetContainer getTopLeft(){
        return topleft;
    }

    public WidgetContainer getBottomRight(){
        return bottomright;
    }

    public WidgetContainer getBottomLeft(){
      return bottomleft;
    }

    public WidgetContainer getCenter(){
        return center;
    }

    protected class LeftJustifiedTopDown extends WidgetContainer {

        public void draw(PApplet p){
            p.pushMatrix();
            for (ArrayList<UIWidget> row : getRows()) {
                p.pushMatrix();
                int rowHeight = 0;

                for (UIWidget widget : row) {
                    p.pushMatrix(); p.pushStyle();
                      widget.draw(p);
                      Dimension size = widget.getSize(p);
                    p.popMatrix(); p.popStyle();
                    p.translate((float) size.getWidth(), 0);
                    rowHeight = Math.max(rowHeight, size.height);
                }

                p.popMatrix();
                p.translate(0, rowHeight);
            }
            p.popMatrix();
        }

    }

    protected class RightJustifiedBottomUp extends WidgetContainer{

        @Override
        public void draw(PApplet p) {
            ArrayList<ArrayList<UIWidget>> rows = new ArrayList<ArrayList<UIWidget>>(getRows());
            Collections.reverse(rows);

            p.pushMatrix();
            for (ArrayList<UIWidget> row : rows ) {
                ArrayList<UIWidget> reversedRow = new ArrayList<UIWidget>(row);
                Collections.reverse(reversedRow);

                p.translate(0, -getRowHeight(p, reversedRow), 0);
                p.pushMatrix(); p.pushStyle();
                for (UIWidget widget : reversedRow) {
                    Dimension size = widget.getSize(p);
                    p.translate(-size.width, 0);
                    p.pushMatrix(); p.pushStyle();
                      widget.draw(p);
                    p.popMatrix(); p.popStyle();
                }
                p.popMatrix(); p.popStyle();

            }
            p.popMatrix();

        }

    }

    protected class LeftJustifiedBottomUp extends WidgetContainer{

        @Override
        public void draw(PApplet p) {
            ArrayList<ArrayList<UIWidget>> rows = new ArrayList<ArrayList<UIWidget>>(getRows());
            Collections.reverse(rows);

            p.pushMatrix();
            for (ArrayList<UIWidget> row : rows) {
                p.translate(0, -getRowHeight(p, row));
                p.pushMatrix();
                for (UIWidget widget : row){
                    p.pushMatrix(); p.pushStyle();
                      widget.draw(p);
                      Dimension size= widget.getSize(p);
                    p.popMatrix(); p.popStyle();
                    p.translate((float)size.getWidth(), 0);
                }
                p.popMatrix();
            }
            p.popMatrix();
        }

    }

}