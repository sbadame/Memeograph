package memeograph.ui;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JFrame;
import memeograph.DiGraph;

public class MemeoFrame extends JFrame{
		
    public MemeoFrame(List<DiGraph> stacks){
        super("Memeographer!");

        setLayout(new BorderLayout());
        MemeoPApplet papplet = new MemeoPApplet(stacks, 1024, 768);
        add(papplet, BorderLayout.CENTER);

        setSize(1024, 768);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        papplet.frame = this;
        papplet.init();
    }

}
