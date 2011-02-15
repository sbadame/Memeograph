package memeograph.renderer.processing;

import memeograph.Config;
import memeograph.Generator;
import memeograph.generator.jdi.InteractiveStep;
import memeograph.generator.jdi.InteractiveStep.Depth;
import memeograph.graph.Graph;
import memeograph.renderer.processing.ui.InteractiveUI;
import memeograph.renderer.processing.ui.UI;

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
             Depth d = null;
             switch(k){
                 case 'i':
                 case 'I': d = Depth.STEP_INTO; break;
                 case 'o':
                 case 'O': d = Depth.STEP_OVER; break;
                 default: super.keyPressed(); return;
             }

             //Run this in it's own thread because we shouldn't do intensive
             //stuff in the UI thread, also because if step crashes, so does
             //the program and that's just dumb.
             final Depth finalDepth = d;
             if (d != null) {
               new Thread(){
                  @Override
                  public void run(){
                    is.step(InteractiveStep.Size.STEP_LINE, finalDepth);
                  }
               }.start();
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

    @Override
    protected UI createUI(){
        return new InteractiveUI(this);
    }
}
