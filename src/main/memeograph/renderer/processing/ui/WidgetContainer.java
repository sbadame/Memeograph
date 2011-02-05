package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import processing.core.PApplet;

public class WidgetContainer extends UIWidget{
  private ArrayList<ArrayList<UIWidget>> rows = new ArrayList<ArrayList<UIWidget>>();
  private ArrayList<UIWidget> column = new ArrayList<UIWidget>();

  public WidgetContainer(){
      rows.add(column);
  }

  public void add(UIWidget w){
      column.add(w);
  }

  public void newRow(){
     ArrayList<UIWidget> newrow = new ArrayList<UIWidget>();
     rows.add(newrow);
     column = newrow;
  }

  @Override
  public Dimension draw(PApplet p) {
      Dimension d = new Dimension(0,0);
      p.pushMatrix();
      for (ArrayList<UIWidget> row : rows) {
          p.pushMatrix();
          int rowWidth = 0;
          int rowHeight = 0;

          for (UIWidget widget : row) {
              p.pushMatrix(); p.pushStyle();
              Dimension2D size = widget.draw(p);
              p.popMatrix(); p.popStyle();

              p.translate((float) size.getWidth(), 0, 0);
              rowWidth += size.getWidth();
              rowHeight = Math.max((int)size.getHeight(), rowHeight);
          }

          d.width = Math.max(d.width, rowWidth);
          d.height += rowHeight;
          p.popMatrix();
          p.translate(0, rowHeight, 0);
      }
      p.popMatrix();
      return d;
  }

}
