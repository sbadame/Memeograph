package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import processing.core.PApplet;

public class TextWidget extends UIWidget{

  private String text = "";

  public TextWidget(String text){
      this.text = text;
  }

  public TextWidget(){

  }

  public String getText(){
      return text;
  }

  public Dimension getSize(PApplet p){
      String s = getText();
      return new Dimension((int)p.textWidth(s), (int)(p.textAscent() + p.textDescent()));
  }

  @Override
  public void draw(PApplet p) {
      p.noFill();
      p.text(getText(), 0, 0, 0);
  }

}
