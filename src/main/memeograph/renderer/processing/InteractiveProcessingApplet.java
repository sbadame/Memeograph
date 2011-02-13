package memeograph.renderer.processing;

import memeograph.Config;
import memeograph.Generator;
import memeograph.generator.jdi.InteractiveStep;
import memeograph.graph.Graph;

public class InteractiveProcessingApplet extends ProcessingApplet{

    private InteractiveStep is = null;

    public InteractiveProcessingApplet(){
        super();

        Generator generator = Config.getConfig().getGenerator();
        if (! (generator instanceof InteractiveStep) ) {
            System.err.println("WARNING: You're using InteractiveProcessingApplet with a generator that isn't InteractiveStep!" );
        }else{
            is = (InteractiveStep)generator;
        }
    }

    @Override
    public void keyPressed(){
        if (currentgraph != null && is != null) {
             char k = (char) key;
             switch(k){
                 case 'i':
                 case 'I': is.step(InteractiveStep.Size.STEP_LINE, InteractiveStep.Depth.STEP_INTO); break;
                 case 'o':
                 case 'O': is.step(InteractiveStep.Size.STEP_LINE, InteractiveStep.Depth.STEP_OVER); break;
                 default: super.keyPressed();
             }
        }else{
            super.keyPressed();
        }
    }

    @Override
    public void addGraph(Graph g){
        super.addGraph(g);
        showNextGraph();
    }
}
