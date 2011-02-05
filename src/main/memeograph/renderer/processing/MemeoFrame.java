package memeograph.renderer.processing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import memeograph.GraphRenderer;
import memeograph.graph.Graph;

public class MemeoFrame extends JFrame implements GraphRenderer{
  
  private JButton savegraph;
  private ProcessingApplet papplet;  
  private final List<Graph> graphs = Collections.synchronizedList(new ArrayList<Graph>());
  
  public MemeoFrame(){
      super("Memeographer!");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  public void init() {
        
        //Make out processing applet
        papplet = new ProcessingApplet();
        papplet.frame = this;
        
        //The save the graph button
        savegraph = new JButton("Save"){{
            setEnabled(false);
            addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {saveGraph();}
            });
        }};

        JPanel topBar = new JPanel();
        topBar.add(savegraph);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        
        setSize(1024, 768);
        setLocationRelativeTo(null); //Centers the frame
        setVisible(true);
        add(papplet, BorderLayout.CENTER);
        papplet.init();
  }

  public void setGraphs(Iterator<Graph> generatorIterator) {
      while(generatorIterator.hasNext()){
          Graph g = generatorIterator.next();
          if (savegraph.isEnabled() == false) {
             savegraph.setEnabled(true);
          }
          graphs.add(g);
          papplet.addGraph(g);
      }
  }
  
  private void saveGraph(){
      
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            File selectedFile = fc.getSelectedFile();
            try {  
              FileOutputStream fos = new FileOutputStream(selectedFile);
              ObjectOutputStream oos = new ObjectOutputStream(fos);
              oos.writeObject(graphs);
              oos.flush();
              oos.close();
            }catch (IOException ex) {
              ex.printStackTrace();
            }
        }

  }

}
