package memeograph.renderer.processing.ui;

import java.awt.Color;
import java.awt.Dimension;
import processing.core.PApplet;

public class TextWidget extends UIWidget{

  private String text = "";

  public TextWidget(String text){
      super();
      this.text = text;
  }

  public TextWidget(String name, String text){
      super(name);
      this.text = text;
  }

  public TextWidget(){
      super();
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
      p.pushStyle();
        p.noFill();
        p.text(getText(), 0, 0);
      p.popStyle();
  }

}
