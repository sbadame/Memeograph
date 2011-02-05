package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import processing.core.PApplet;

public abstract class UIWidget {

    public abstract void draw(PApplet p);
    public abstract Dimension getSize(PApplet p);
}
