package memeograph.generator.jdi;

import memeograph.Config;
import memeograph.graph.Graph;

public class StepLineJDI extends InteractiveStep {

    public StepLineJDI(Config c){
        super(c);
    }

    @Override
    public Graph getNextGraph(){
        super.step(Size.STEP_LINE, Depth.STEP_OVER);
        return super.getNextGraph();
    }

}
