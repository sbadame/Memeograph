package memeograph.generator.jdb;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import memeograph.GraphGenerator;
import memeograph.Config;
import memeograph.graph.Graph;

/**
 * This will interrogate a VM for an object graph whenever memeograph.step()
 * is called in the SuT.
 *
 * Has the slightly strange sideeffect that if "jdbgrapher.savefile" is set it
 * will serialize the graph and save it to the filename pointed to by the property.
 * that graph can then be loaded using the GraphFileLoader which is much faster
 * then re-running the program and building the graph from scratch in most
 * cases.
 */
public class JDBGraphGenerator implements GraphGenerator {

  private VirtualMachine virtualMachine;
  private Config config;

  public JDBGraphGenerator(Config config){
    this.config = config;
  }

  @Override
  public void start(){
      String target_args = config.getProperty(Config.VM_OPTIONS, "");
      String target = target_args.substring(target_args.lastIndexOf(' '));
      target_args = target_args.substring(0, target_args.lastIndexOf(' '));
      if (target != null) {
        virtualMachine = createTargetVM(target, target_args);
      }
  }

  @Override
  public Iterator<Graph> getGraphs() {
    return new VMParser(virtualMachine).getGraphs();
  }

  private static VirtualMachine createTargetVM(String main_command, String vm_options){
      for (LaunchingConnector connector : Bootstrap.virtualMachineManager().launchingConnectors()) {
              if (connector.transport().name().equals("dt_socket") == false) {
                  continue;
              }
              Map<String, Argument> launchargs = connector.defaultArguments();
              launchargs.get("options").setValue(vm_options);
              launchargs.get("main").setValue(main_command);
              try {
                  final VirtualMachine vm = connector.launch(launchargs);
                  new ProcessDirector(vm.process()).start();
                  return vm;
              } catch (IOException ex) {
                ex.printStackTrace();
              } catch (IllegalConnectorArgumentsException ex) {
                ex.printStackTrace();
              } catch (VMStartException ex) {
                ex.printStackTrace();
              }
      }
      return null;
  }
}
