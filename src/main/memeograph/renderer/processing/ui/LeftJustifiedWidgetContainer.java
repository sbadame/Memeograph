package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import processing.core.PApplet;

public class LeftJustifiedWidgetContainer extends WidgetContainer {

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
