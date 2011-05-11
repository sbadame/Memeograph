package memeograph.renderer.processing;

import javax.swing.*;
import java.util.*;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import memeograph.Config;
import memeograph.Renderer;
import memeograph.generator.filebuilder.GraphFileLoader;
import memeograph.graph.Graph;

public class MemeoFrame extends JFrame implements Renderer{

    private JButton savegraph, loadgraph, graphit;
    private JTextField method, program;
    private ProcessingApplet papplet;
    private final List<Graph> graphs = Collections.synchronizedList(new ArrayList<Graph>());
    private final Object waitLock = new Object();
    
    public MemeoFrame(){
        super("Memeographer!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public void init(){}
    public void init(Config c) {
        final Config config = c;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Couldn't load the Native Look and Feel.");
            ex.printStackTrace();
        }

        //Make out processing applet
        papplet = getGraphDisplayer();

        //The load the graph button
        loadgraph = new JButton("Open a Saved Graph"){{
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
        
        method = new JTextField(config.getProperty(config.TRIGGER),25);
        
        program = new JTextField(config.getProperty(config.TARGET_OPTIONS),25);
        
        graphit = new JButton("Run Memeographer!"){{
            addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) { synchronized(waitLock){waitLock.notify();} }
            });
        }};

        final JPanel topBar = new JPanel();
        topBar.setOpaque(true);
        topBar.add(method);
        topBar.add(graphit);
        topBar.add(loadgraph);
        topBar.add(program);

        
        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);

        //The only way that is actually aware of multiple monitors...
        int width = 800; //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
        int height = 600; //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
        width -= 20;
        height -= 40;
        setSize(width, height);
        setLocationRelativeTo(null);
        add(papplet, BorderLayout.CENTER);
        papplet.setSize(width, height);
        papplet.init();
        setVisible(true);
        
        synchronized(waitLock){
            try{
                waitLock.wait();
            } catch(InterruptedException ex){
                ex.printStackTrace();
            }
        }
        
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              config.setProperty(config.TRIGGER,method.getText()); 
              config.setProperty(config.TARGET_OPTIONS,program.getText());
              topBar.remove(loadgraph);
              topBar.remove(graphit);
              topBar.remove(method);
              topBar.remove(program);
              topBar.add(savegraph);
              topBar.validate();
              topBar.repaint();
            }
        });

    }

    public void addGraph(Graph g) {
        if (g == null) { return; }
        graphs.add(g);
        papplet.addGraph(g);
    }

    public void finish(){}

    public ProcessingApplet getGraphDisplayer(){
        ProcessingApplet pApplet = new ProcessingApplet();
        pApplet.frame = this;
        return pApplet;
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
