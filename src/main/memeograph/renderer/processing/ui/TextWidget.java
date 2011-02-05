package memeograph.renderer.processing.ui;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import processing.core.PApplet;

public class TextWidget extends UIWidget{

  private TextMaker textmaker;

  public TextWidget(final String text){
     this(new TextMaker(){
       public String getText(){
          return text;
       }
     });
  }

  public TextWidget(TextMaker tm){
     this.textmaker = tm;
  }

  @Override
  public Dimension2D draw(PApplet p) {
      String s = textmaker.getText();
      Dimension dimension = new Dimension((int) p.textWidth(s), (int) (p.textAscent() + p.textDescent()));
      p.text(s, 0, 0);
      return dimension;
  }

}
