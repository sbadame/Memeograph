package memeograph.generator.jdi;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import memeograph.Config;
import memeograph.graph.Graph;

public class MethodTriggerJDI extends JDI{
    private final String triggermethodname;
    private final String triggerclassname;

    public MethodTriggerJDI(Config c){
        super(c);

        String t = Config.getConfig().getProperty(Config.TRIGGER);
        triggermethodname = t.substring(t.lastIndexOf('.')+1);
        triggerclassname = t.substring(0, t.lastIndexOf('.'));
    }

    @Override
    public void startupEventRequests(){
        //Lets listen for a step...
        MethodEntryRequest mer = getVirtualMachine().eventRequestManager().createMethodEntryRequest();
        mer.addClassFilter(triggerclassname);
        mer.setSuspendPolicy(EventRequest.SUSPEND_ALL);

        addVMEventListener(mer, new EventAction(){
            public Graph doAction(Event event) {
                MethodEntryEvent mee = (MethodEntryEvent)event;
                if (mee.method().name().equals(triggermethodname)) {
                    return generateGraph();
                }
                return null;
            }
        });

        mer.enable();
    }

}
