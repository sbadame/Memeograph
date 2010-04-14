package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    public static void main(String[] args) 
    {
        //Step 1 - Connect to our target program
        VirtualMachine vm;
        if(args.length == 1 && args[0].matches("^\\d+$")){ //user specificed a port
           vm = connectToTargetVM(args[0]);
        }else{
           vm = createTargetVM(args);
        }

//      try {
//          Thread.sleep(2000);
//      } catch (InterruptedException ioe) {}
//      if (vm == null)
//          throw new Error("No VM was found");
//
        //Step 2 - Get a Graph
        GraphBuilder grapher = new GraphBuilder(vm);

        //Step 3 - Render the graph
        if (args != null && args.length > 0 && args[0].equals("dot")){
            outputDot(grapher);
        } else {
            new MemeoFrame(grapher);
        }
    }

    private static VirtualMachine createTargetVM(String[] args){
        for (LaunchingConnector connector : Bootstrap.virtualMachineManager().launchingConnectors()) {
                if (connector.transport().name().equals("dt_socket") == false) {
                    continue;
                }
                Map<String, Argument> launchargs = connector.defaultArguments();
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
                                }
                            } catch (IOException ex) { }
                        }
                    }.start();
                    return vm;
                } catch (IOException ex) {
                } catch (IllegalConnectorArgumentsException ex) {
                } catch (VMStartException ex) {
                }
        }
        return null;
    }

    private static VirtualMachine connectToTargetVM(String port){
        Iterator<AttachingConnector> iterator = Bootstrap.virtualMachineManager().attachingConnectors().iterator();
        while(iterator.hasNext()){
            AttachingConnector c = iterator.next();
            if (c.name().equals("com.sun.jdi.SocketAttach")){
                Map<String, Argument> defaultArguments = c.defaultArguments();
                defaultArguments.get("port").setValue(port);
                try {
                    return c.attach(defaultArguments);
                } catch (IOException ex) {
                    throw new Error("Unable to attach to target VM", ex);
                } catch (IllegalConnectorArgumentsException ex) {
                    throw new Error("Bad Arguments: " + ex.argumentNames(), ex);
                }
            }
        }
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
