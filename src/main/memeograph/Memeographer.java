package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import memeograph.graphics.GraphFrame;

/**
 * Starting off the begining of the Memeographer. This class starts the show.
 * The program goes through the following steps:
 *   1.) Connect to a running java progam on port 8000
 *	 2.) Go through its threads and loaded class to generate our grapher that
 *			 has the text and relationships that we want to be sown
 *	 3.) Pass the Graph to a class that can render it.
 */
public class Memeographer {
    // We are assuming that the target vm was started with 'transport=dt_socket'
    private static final String TRANSPORT = "com.sun.jdi.SocketAttach";

    // We are also assuming that the target is waiting for us on port 8000
    private static final String PORT = "8000";


    public static void main(String[] args) 
    { 

			  //Step 1 - Connect to our target program
        VirtualMachine vm = getTargetVM();
        if (vm == null)
            throw new Error("No VM was found");

				//Step 2 - Get a Graph
				GraphBuilder grapher = new GraphBuilder(vm);
				grapher.buildGraph();
				Tree graph = grapher.getGraph();

				//Step 3 - Render the graph
				if (args != null && args.length > 0 && args[0].equals("dot")){
						outputDot(grapher);
				}else{
						GraphFrame frame = new GraphFrame(graph);
						frame.setVisible(true);
				}
		}

    private static VirtualMachine getTargetVM(){
        List connectors = Bootstrap.virtualMachineManager().attachingConnectors();
        Iterator i = connectors.iterator();
        while(i.hasNext()){
            Connector c = (Connector)i.next();
            if (c.name().equals(TRANSPORT)) {
                AttachingConnector ac = (AttachingConnector)c;
                try {
                    Map<String, Argument> args = ac.defaultArguments();
                    args.get("port").setValue(PORT);
                    return ac.attach(args);
                } catch (IOException ex) {
                    throw new Error("Unable to attach to target VM: " + ex);
                } catch (IllegalConnectorArgumentsException ex) {
                    System.err.println("Bad Arguments: " + ex.argumentNames());
                    throw new Error("Bad Connector Arguments: " + ex);
                }
            }
        }

        return null;
    }

		private static void outputDot(GraphBuilder grapher){
				HashMap<String, Tree> graphMap = grapher.getGraphMap();
        try {
            PrintWriter out = new PrintWriter(new FileWriter("output.dot"), true);
            out.println("digraph memeograph {");

						for (Tree t : graphMap.values()) {
								for (Tree child : t.getChildren()) {
                    out.println("  \"" + t.getData() + "\" -> \"" + child.getData() + "\";");
								}
						}

            out.println("}");
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
		}

}
