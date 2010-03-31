package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import memeograph.ui.MemeoFrame;

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
    private static String PORT = "8000";


    public static void main(String[] args) 
    {
        System.out.print("Args received: ");
        for (String arg : args) {
            System.out.print(arg +  " ");
        }
        System.out.println();

        //Step 1 - Connect to our target program
        VirtualMachine vm = createTargetVM(args);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ioe) {}
        if (vm == null)
            throw new Error("No VM was found");

        //Step 2 - Get a Graph
        GraphBuilder grapher = new GraphBuilder(vm);
        System.out.println("Building the Graph!");
        grapher.buildGraph();
        Vector<DiGraph> stacks = grapher.getStacks();

        //Step 3 - Render the graph
        if (args != null && args.length > 0 && args[0].equals("dot")){
            outputDot(grapher);
        } else {
            MemeoFrame frame = new MemeoFrame(stacks);

            }
        }

    private static VirtualMachine createTargetVM(String[] args){
        for (LaunchingConnector connector : Bootstrap.virtualMachineManager().launchingConnectors()) {
                if (connector.transport().name().equals("dt_socket") == false) {
                    continue;
                }
                Map<String, Argument> launchargs = connector.defaultArguments();
                System.out.println(launchargs.keySet());
                String options = "";
                for(int i = 0; i < args.length - 2; i++){
                   options += args[i] + " ";
                }
                launchargs.get("options").setValue(options);
                launchargs.get("main").setValue(args[args.length-1]);
                try {
                    final VirtualMachine vm = connector.launch(launchargs);
                    new Thread(){
                        @Override
                        public void run(){
                            Process process = vm.process();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                    process.getInputStream()));
                            String s;
                            try {
                                while ((s = reader.readLine()) != null) {
                                    System.out.println(s);
                                }
                            } catch (IOException ex) { }
                        }
                    }.start();

                    new Thread(){
                        @Override
                        public void run(){
                            Process process = vm.process();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                    process.getErrorStream()));
                            String s;
                            try {
                                while ((s = reader.readLine()) != null) {
                                    System.err.println(s);
                                }
                            } catch (IOException ex) { }
                        }
                    }.start();
                    vm.resume();
                    return vm;
                } catch (IOException ex) {
                } catch (IllegalConnectorArgumentsException ex) {
                } catch (VMStartException ex) {
                }
        }
        /*List connectors = Bootstrap.virtualMachineManager().attachingConnectors();
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
*/
        return null;
    }

    private static void outputDot(GraphBuilder grapher) {
        HashMap<String, DiGraph> graphMap = grapher.getGraphMap();
        try {
            PrintWriter out = new PrintWriter(new FileWriter("output.dot"), true);
            out.println("digraph memeograph {");

            for (DiGraph t : graphMap.values()) {
                for (DiGraph child : t.getSoftwareChildren()) {
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
