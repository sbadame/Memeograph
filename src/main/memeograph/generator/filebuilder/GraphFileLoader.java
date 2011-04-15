package memeograph.generator.filebuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import memeograph.Config;
import memeograph.Generator;
import memeograph.graph.Graph;

/**
 *  Loads a set of ObjectGraphs from a file saved by the JDBGraphGenerator.
 */
public class GraphFileLoader implements Generator{

  public static final String FILE_OPTION = "fileloader.file";

  private File file;
  private List<Graph> graphs = new ArrayList<Graph>();

  public GraphFileLoader(Config c){
      if (c.isPropertySet(FILE_OPTION)) {
        file = new File(c.getProperty(FILE_OPTION));
      }
  }

  @SuppressWarnings("unchecked")
  public void start() {
      if (file == null) {
          System.out.println("No file specificied to be loaded...");
      }
      try {
          FileInputStream fileInputStream = new FileInputStream(file);
          ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
          Object loadedGraph = objectInputStream.readObject();
          if (loadedGraph instanceof List) {
             graphs = (List<Graph>) loadedGraph;
          }else{
            throw new ClassCastException("The file loaded does not contain a list of Graphs, expected: List<Graph>");
          }
      } catch (FileNotFoundException ex) {
          ex.printStackTrace();
      } catch (IOException ex) {
          ex.printStackTrace();
      } catch (ClassNotFoundException ex) {
          ex.printStackTrace();
      }
  }

  public boolean isAlive() {
      return !graphs.isEmpty();
  }

  public Iterator<Graph> getGraphs() {
      return graphs.iterator();
  }

}
