package memeograph.renderer.processing.ui;

import memeograph.renderer.processing.ProcessingApplet;
import processing.core.PApplet;

public class UI {
    
    private final ProcessingApplet p;
    private final int PADDING = 5;

    private WidgetContainer topleft;
    private WidgetContainer bottomright;

    public UI(ProcessingApplet pApplet){
        this.p = pApplet;
    }

    public void init(){
        topleft = getTopLeft();
        bottomright = getBottomRight();
    }

    public void draw(){
        p.hint(PApplet.DISABLE_DEPTH_TEST);
        p.camera();
        p.textAlign(PApplet.LEFT, PApplet.TOP);

        p.translate(PADDING,PADDING,0);
        topleft.draw(p);
        p.translate(p.width-PADDING-2, p.height-PADDING, 0);
        bottomright.draw(p);
    }

    public WidgetContainer getTopLeft(){
        return new LeftJustifiedWidgetContainer(){{
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
        return new RightJustifiedWidgetContainer(){{
            add(new TextWidget("Controls"));
            newRow();
            add(new TextWidget("N : next graph"));
            newRow();
            add(new TextWidget("Mouse Wheel: zoom out"));
        }};
    }
}
