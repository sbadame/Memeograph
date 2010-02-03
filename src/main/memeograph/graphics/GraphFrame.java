package memeograph.graphics;

import java.awt.GridLayout;
import javax.swing.JFrame;
import memeograph.Tree;

public class GraphFrame extends JFrame{

		private Tree tree;
		private TreeDisplay display;

		public GraphFrame(Tree tree){
				super("Memoeographer");
				this.tree = tree;

				setLayout(new GridLayout(1, 1)); //Just make our display as big as 
																				 //possible
				display = new TreeDisplay();
				display.setTree(tree);

				add(display);

				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				pack();
				setLocationRelativeTo(null);
				setSize(800,600);
		}



}
