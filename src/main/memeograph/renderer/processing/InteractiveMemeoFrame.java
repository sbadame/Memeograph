package memeograph.renderer.processing;

public class InteractiveMemeoFrame extends MemeoFrame{

    @Override
    public ProcessingApplet getGraphDisplayer(){
        InteractiveProcessingApplet p = new InteractiveProcessingApplet();
        p.frame = this;
        return p;
    }
}
