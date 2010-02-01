package memeograph;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starting off the begining of the Memeographer. This class starts the show.
 * 
 */
public class Memeographer {
    // We are assuming that the target vm was started with 'transport=dt_socket'
    private static final String TRANSPORT = "com.sun.jdi.SocketAttach";

    // We are also assuming that the target is waiting for us on port 8000
    private static final String PORT = "8000";

    private static HashMap<String, ArrayList<String>> hashMap = new HashMap<String, ArrayList<String>>();

    public static void main(String[] args) 
    { 
        VirtualMachine vm = getTargetVM();
        if (vm == null)
            throw new Error("No VM was found");
        vm.suspend();

        for(ReferenceType t : vm.allClasses()){
            searchType(t);
        }

        for (ThreadReference tr : vm.allThreads()) {
            int sfcount = 0;
            try {
                for (StackFrame sf : tr.frames()) {
                    hashMap.put("StackFrame" + sfcount, new ArrayList<String>());
                    if (sfcount > 0) {
                        hashMap.get("StackFrame" + (sfcount-1)).add("StackFrame" + sfcount);
                    }

                    try {
                        for (LocalVariable lv : sf.visibleVariables()) {
                            Value val = sf.getValue(lv);
                            if (val != null && val.type() != null && val.type() instanceof ClassType){
                                ObjectReference or = (ObjectReference)val;
                                hashMap.get("StackFrame" + sfcount).add("" + or.referenceType().name() + "<" + or.uniqueID() + ">");
                            }
                        }
                    } catch (AbsentInformationException aie) {
                        System.out.println("No such info for stack frame " + sfcount);
                    }
                    sfcount++;
                }
            } catch (IncompatibleThreadStateException itse) {
                System.out.println("Why in the world do we have an IncompatibleThreadStateException?");
                itse.printStackTrace();
            }
        }

        for (ThreadReference tr : vm.allThreads()) tr.resume();

        try {
            PrintWriter out = new PrintWriter(new FileWriter("output.dot"), true);
            out.println("digraph memeograph {");
            for (String object : hashMap.keySet()) {
                for (String ref : hashMap.get(object)) {
                    out.println("  \"" + object + "\" -> \"" + ref + "\";");
                }
            }
            out.println("}");
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        vm.resume();
    }

    private static void searchType(ReferenceType t){
        for (ObjectReference o : t.instances(0)) {
            if (o.referenceType().name().startsWith("java.")) continue;
            if (o.referenceType().name().startsWith("sun.")) continue;

            ArrayList<String> list = new ArrayList<String>();
            for (Value val : o.getValues(t.allFields()).values() ){
                if (val != null && val.type() != null && val.type() instanceof ClassType){
                    ObjectReference or = (ObjectReference)val;
                    list.add("" + or.referenceType().name() + "<" + or.uniqueID() + ">");
                }
            }
            hashMap.put("" + o.referenceType().name() + "<" + o.uniqueID() + ">", list);
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

}
