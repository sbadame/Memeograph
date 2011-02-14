package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import memeograph.renderer.processing.ProcessingApplet;
import processing.core.PApplet;

public class UI {
    
    public final ProcessingApplet p;
    private final int PADDING = 5;

    private WidgetContainer topleft;
    private WidgetContainer bottomright;
    private WidgetContainer bottomleft;

    public UI(ProcessingApplet pApplet){
        this.p = pApplet;
    }

    public void init(){
        topleft = getTopLeft();
        bottomright = getBottomRight();
        bottomleft = getBottomLeft();
    }

    public void draw(){
        p.hint(PApplet.DISABLE_DEPTH_TEST);
        p.camera();
        p.textAlign(PApplet.LEFT, PApplet.TOP);

        if (topleft != null) {
            p.pushMatrix();
                p.translate(PADDING,PADDING,0);
                topleft.draw(p);
            p.popMatrix();
        }

        if (bottomright != null) {
            p.pushMatrix();
                p.translate(p.width-PADDING, p.height-PADDING, 0);
                bottomright.draw(p);
            p.popMatrix();
        }

        if (bottomleft != null) {
            p.pushMatrix();
                p.translate(0, p.height-PADDING, 0);
                bottomleft.draw(p);
            p.popMatrix();
        }
    }

    public WidgetContainer getTopLeft(){
        return new LeftJustifiedTopDown(){{
            add(new TextWidget(){
                @Override
                public String getText() { return "FPS: " + p.round(p.frameRate); }
            });

            newRow();

            add(new TextWidget(){
                @Override
                public String getText() { return "Graph " + (p.getGraphs().indexOf(p.getCurrentGraph()) + 1) + " of " + p.getGraphs().size();}
            });
        }};
    };

    public WidgetContainer getBottomRight(){
        return new RightJustifiedBottomUp(){{
            add(new TextWidget("Controls"));
            newRow();
            add(new TextWidget("N : next graph"));
            newRow();
            add(new TextWidget("T : toggle text"));
            newRow();
            add(new TextWidget("Mouse Wheel: zoom in/out"));
        }};
    }

    public WidgetContainer getBottomLeft(){return null;}


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
                    p.translate((float) size.getWidth(), 0, 0);
                    rowHeight = Math.max(rowHeight, size.height);
                }

                p.popMatrix();
                p.translate(0, rowHeight, 0);
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
                    p.translate(-size.width, 0, 0);
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
                p.translate(0, -getRowHeight(p, row), 0);
                p.pushMatrix();
                for (UIWidget widget : row){
                    p.pushMatrix(); p.pushStyle();
                      widget.draw(p);
                      Dimension size= widget.getSize(p);
                    p.popMatrix(); p.popStyle();
                    p.translate((float)size.getWidth(), 0, 0);
                }
                p.popMatrix();
            }
            p.popMatrix();
        }

    }

}
