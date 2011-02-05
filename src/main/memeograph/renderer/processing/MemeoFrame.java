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
import memeograph.Config;
import memeograph.GraphRenderer;
import memeograph.generator.filebuilder.GraphFileLoader;
import memeograph.graph.Graph;

public class MemeoFrame extends JFrame implements GraphRenderer{
  
  private JButton savegraph, loadgraph, graphit;
  private ProcessingApplet papplet;  
  private final List<Graph> graphs = Collections.synchronizedList(new ArrayList<Graph>());

  private final Object waitLock = new Object();
  
  public MemeoFrame(){
      super("Memeographer!");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  public void init() {
        
        //Make out processing applet
        papplet = new ProcessingApplet();
        papplet.frame = this;
        
        //The load the graph button
        loadgraph = new JButton("Load a graph"){{
            addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {loadGraph();}
            });
        }};

        //The save the graph button
        savegraph = new JButton("Save"){{
            addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {saveGraph();}
            });
        }};

        graphit = new JButton("Run it!"){{
            addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) { doGraph(); }
            });
        }};

        final JPanel topBar = new JPanel();
        topBar.setOpaque(true);
        topBar.add(graphit);
        topBar.add(loadgraph);

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        
        setSize(1024, 768);
        setLocationRelativeTo(null); //Centers the frame
        setVisible(true);
        add(papplet, BorderLayout.CENTER);
        papplet.init();

        synchronized(waitLock) {
          try {
            waitLock.wait();
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }

        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              topBar.remove(loadgraph);
              topBar.remove(graphit);
              topBar.add(savegraph);
              topBar.validate();
              topBar.repaint();
            }
        });
  }

  public void setGraphs(Iterator<Graph> generatorIterator) {
      while(generatorIterator.hasNext()){
          Graph g = generatorIterator.next();
          graphs.add(g);
          papplet.addGraph(g);
      }
  }

  private void doGraph(){
    synchronized(waitLock){
      waitLock.notify();
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

  private void loadGraph(){
      JFileChooser fc = new JFileChooser();
      if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fc.getSelectedFile();
          Config.getConfig().setProperty(Config.GENERATOR, GraphFileLoader.class.getCanonicalName());
          Config.getConfig().setProperty(GraphFileLoader.FILE_OPTION, selectedFile.getAbsolutePath());
      }
      synchronized (waitLock){ waitLock.notify(); }
  }

}
