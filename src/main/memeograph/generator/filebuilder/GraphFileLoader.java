package memeograph.generator.filebuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import memeograph.Config;
import memeograph.GraphGenerator;
import memeograph.graph.Graph;

/**
 *  Loads a set of ObjectGraphs from a file saved by the JDBGraphGenerator.
 */
public class GraphFileLoader implements GraphGenerator{

  public static final String FILE_OPTION = "fileloader.file";

  private File file;

  public GraphFileLoader(Config c){
      if (c.isPropertySet(FILE_OPTION)) {
        file = new File(c.getProperty(FILE_OPTION));
      }
  }

  public void start() {
      if (file == null) {
          System.out.println("No file specificied to be loaded...");
      }
  }

  @SuppressWarnings("unchecked")
  public Iterator<Graph> getGraphs() {
    try {
      FileInputStream fileInputStream = new FileInputStream(file);
      ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
      Object loadedGraph = objectInputStream.readObject();
      if (loadedGraph instanceof List) {
        List<Graph> list = (List<Graph>) loadedGraph;
        return list.iterator();
      }else{
        throw new ClassCastException("The file loaded does not contain a list of Graphs, expected: List<Graph>");
      }
    } catch (FileNotFoundException ex) {
      Logger.getLogger(GraphFileLoader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(GraphFileLoader.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(GraphFileLoader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new ArrayList<Graph>().iterator();
  }

}
