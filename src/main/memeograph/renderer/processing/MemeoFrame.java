package memeograph.renderer.processing;

import javax.swing.*;
import java.util.*;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import memeograph.Renderer;
import memeograph.graph.Graph;

public class MemeoFrame extends JFrame implements Renderer{

    private ProcessingApplet papplet;
    private final List<Graph> graphs = Collections.synchronizedList(new ArrayList<Graph>());


    public MemeoFrame(){
        super("Memeographer!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Couldn't load the Native Look and Feel.");
            ex.printStackTrace();
        }

        //Make out processing applet
        papplet = getGraphDisplayer();

        setLayout(new BorderLayout());

        setVisible(true);
        add(papplet, BorderLayout.CENTER);

        //The only way that is actually aware of multiple monitors...
        int width = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
        int height = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
        width -= 20;
        height -= 40;
        setSize(width, height);
        setLocationRelativeTo(null);

        papplet.init();
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
}