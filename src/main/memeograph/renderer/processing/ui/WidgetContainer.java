package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import processing.core.PApplet;

public abstract class WidgetContainer extends UIWidget{
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

  public ArrayList<ArrayList<UIWidget>> getRows(){
      return rows;
  }

  public int getRowHeight(PApplet p, ArrayList<UIWidget> row){
      int height = 0;
      for (UIWidget w : row) {
          height = Math.max(height, w.getSize(p).height);
      }
      return height;
  }

  public Dimension getSize(PApplet p){
      int width = 0;
      int height = 0;

      for (ArrayList<UIWidget> row : rows) {
          int rowWidth = 0;
          int rowHeight = 0;
          for (UIWidget w : row) {
              Dimension size = w.getSize(p);
              rowWidth += size.width;
              rowHeight = Math.max(rowHeight, size.height);
          }
          width = Math.max(rowWidth, width);
          height += rowHeight;
      }

      return new Dimension(width, height);
  }

}
