package memeograph.ui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import memeograph.Tree;

public class MemeoFrame extends JFrame{
		
    public MemeoFrame(Tree graph){
        super("Memeographer!");

        MemeoPApplet papplet = new MemeoPApplet(graph);

        setLayout(new BorderLayout());
        add(papplet, BorderLayout.CENTER);

        papplet.init();

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
