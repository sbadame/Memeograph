package memeograph.renderer.processing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JLabel;
import memeograph.GraphRenderer;
import memeograph.graph.Graph;

public class MemeoFrame extends JFrame implements GraphRenderer{

  JLabel creatingGraph = new JLabel("Creating graph..."){{
      setBackground(Color.WHITE);
      setForeground(Color.BLACK);
  }};
		
  public MemeoFrame(){
      super("Memeographer!");
  }

  public void init() {
        setLayout(new BorderLayout());
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public void setGraphs(Iterator<Graph> graphs) {
        ProcessingApplet papplet = new ProcessingApplet();
        add(papplet, BorderLayout.CENTER);
        papplet.frame = this;
        papplet.init();
        papplet.setGraphs(graphs);
  }

}
