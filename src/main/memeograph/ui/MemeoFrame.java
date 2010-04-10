package memeograph.ui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import memeograph.GraphBuilder;

public class MemeoFrame extends JFrame{
		
    public MemeoFrame(GraphBuilder grapher){
        super("Memeographer!");

        setLayout(new BorderLayout());
        MemeoPApplet papplet = new MemeoPApplet(grapher, 1024, 768);
        add(papplet, BorderLayout.CENTER);

        setSize(1024, 768);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        papplet.frame = this;
        papplet.init();
    }

}
