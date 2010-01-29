package memeograph;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Starting off the begining of the Memeographer. This class starts the show.
 * 
 */
public class Memeographer {
	// We are assuming that the target vm was started with 'transport=dt_socket'
	private static final String TRANSPORT = "com.sun.jdi.SocketAttach";

	// We are also assuming that the target is waiting for us on port 8000
	private static final String PORT = "8000";

	public static void main(String[] args){
		VirtualMachine vm = getTargetVM();
		if (vm == null)
			throw new Error("No VM was found");
		for(ReferenceType t : vm.allClasses()){
			System.out.println(t.name()+ " - " + t.instances(100).size());
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
