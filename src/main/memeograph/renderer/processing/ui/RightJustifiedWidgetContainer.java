package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class RightJustifiedWidgetContainer extends WidgetContainer{

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
