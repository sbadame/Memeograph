package memeograph.generator.jdi;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import memeograph.Config;
import memeograph.graph.Graph;


/**
 * An Interactive graph generator that expects input as to what its next
 * step should be.
 *
 * nextGraph() will only return after a call to step() is made to decide the
 * next step.
 * @author Sandro Badame <a href="mailto:s.badame@gmail.com">s.badame&amp;gmail.com</a>
 */
public class InteractiveStep extends JDI{
    
    public enum Depth {
        STEP_INTO(StepRequest.STEP_INTO),
        STEP_OVER(StepRequest.STEP_OVER),
        STEP_OUT(StepRequest.STEP_OUT);

        private int value;
        Depth(int val){value = val;}
        public int value(){ return value; }
    }

    public enum Size {
        STEP_MIN(StepRequest.STEP_MIN),
        STEP_LINE(StepRequest.STEP_LINE);

        private int value;
        Size(int val){value = val;}
        public int value(){ return value; }
    }

    private boolean hasNextStep = false;
    private final Object stepLock = new Object();

    public InteractiveStep(Config c){
        super(c);
    }

    public void step(Size s, Depth d){
        StepRequest sr = getVirtualMachine().eventRequestManager().createStepRequest(getMainThread(), s.value(), d.value() );
        sr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        sr.addClassFilter(Config.getConfig().getProperty(Config.TARGET_MAIN));
        sr.addCountFilter(1);

        addVMEventListener(sr, new EventAction() {
            public Graph doAction(Event e) {
                e.request().disable();
                return generateGraph();
            }
        });

        sr.enable();

        synchronized(stepLock){hasNextStep = true; stepLock.notify();}
    }

    @Override
    public Graph getNextGraph(){
       synchronized(stepLock){
           while(hasNextStep == false){
               try {
                   stepLock.wait();
               } catch (InterruptedException ex) {
                   ex.printStackTrace();
               }
           }
           Graph g = super.getNextGraph();
           hasNextStep = false;
           return g;
       }
    }


    @Override
    public void VMStarted(){
        step(Size.STEP_MIN, Depth.STEP_INTO);
    }
}
