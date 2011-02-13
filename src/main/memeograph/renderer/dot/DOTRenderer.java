package memeograph.renderer.dot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import memeograph.Config;
import memeograph.Renderer;
import memeograph.generator.jdi.nodes.GraphNodeType;
import memeograph.graph.Graph;
import memeograph.graph.Node;
import memeograph.util.ACyclicIterator;

/**
 * Outputs a graph using dot format. If the "dotdisplayer.save" property is
 * set, the output will be saved the file pointed to in the property. If the
 * "dotdisplayer.save" property is not set then, then the output is printed
 * to standard out.
 *
 * A cute feature for linux/gnome users is to set "dotdisplayer.show" to true
 * and install dot along with gnome-open. The renderer will then automatically
 * compile the output and open the pdf for you.
 */
public class DOTRenderer implements Renderer{

  private boolean show = true;
  private ArrayList<Graph> graphs = new ArrayList<Graph>();

  public DOTRenderer(Config c){
    show = c.isSwitchSet("dotdisplayer.show", Boolean.TRUE);
  }

  public void init() { }

  public void addGraph(Graph g){
    graphs.add(g);
  }

  public void finish() {
    StringBuilder sb = new StringBuilder("digraph Memeograph {");
    for (Graph graph : graphs) {
      Iterator<Node> aCyclicTraversal = new ACyclicIterator<Node>(graph.preorderTraversal());
      while(aCyclicTraversal.hasNext()){
        Node n = aCyclicTraversal.next();
        GraphNodeType type = n.lookup(GraphNodeType.class);
        String id = type.getUniqueID();
        sb.append("\t\"").append(id).append("\" [label=\"").append(type).append("\"];\n");
        for (Node node : n.getChildren()) {
          if (node == null) { continue;}
          sb.append("\t\"").append(id).append("\" -> \"").append(node.lookup(GraphNodeType.class).getUniqueID()).append("\"\n");
        }
      }
    }

    sb.append("}");

    if (show && System.getProperty("os.name").equalsIgnoreCase("Linux")) {
      try {
        File output = File.createTempFile("memeo", ".dot");
        PrintWriter printWriter = new PrintWriter(output);
        printWriter.append(sb);
        printWriter.flush();
        printWriter.close();

        File pdf = File.createTempFile("memeo", ".pdf");
        String name = pdf.getAbsolutePath();
        pdf.delete();
        ProcessBuilder pb = new ProcessBuilder("dot", "-Tpdf", output.getAbsolutePath(), "-o", name);
        Process dot = pb.start();
        try {
          dot.waitFor();
          System.out.println("File created: " + name);
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }

        ProcessBuilder pb2 = new ProcessBuilder("gnome-open", name);
        pb2.start();

      } catch (IOException ex) {
        ex.printStackTrace();
        System.out.println(sb.toString());
      }
    }else{
      System.out.println(sb.toString());
    }

  }

}
