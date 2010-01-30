package memeograph;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
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

	private static HashMap<Long, ArrayList<Long>> hashMap = new HashMap<Long, ArrayList<Long>>();

	public static void main(String[] args){
		VirtualMachine vm = getTargetVM();
		if (vm == null)
			throw new Error("No VM was found");
		for(ReferenceType t : vm.allClasses()){
			searchType(t);
		}
		try {
			PrintWriter out = new PrintWriter(new FileWriter("output.dot"), true);
			out.println("graph memeograph {");
			for (Long object : hashMap.keySet()) {
				for (Long ref : hashMap.get(object)) {
					out.println("  " + object + " -> " + ref + ";");
				}
			}
			out.println("}");
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void searchType(ReferenceType t){
		for (ObjectReference o : t.instances(0)) {
			ArrayList<Long> list = new ArrayList<Long>();
			for (Value val : o.getValues(t.allFields()).values() ){
				if (val != null && val.type() != null && val.type() instanceof ClassType){
					list.add(((ObjectReference)val).uniqueID());
				}
			}
			hashMap.put(o.uniqueID(), list);
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
