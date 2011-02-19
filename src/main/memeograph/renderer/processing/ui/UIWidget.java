package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import processing.core.PApplet;

public abstract class UIWidget {
    private final String name;

    public UIWidget(){
        name = "";
    }

    public UIWidget(String name){
        this.name = name;
    }

    public abstract void draw(PApplet p);
    public abstract Dimension getSize(PApplet p);

    public String getName(){
        return name;
    }
}
