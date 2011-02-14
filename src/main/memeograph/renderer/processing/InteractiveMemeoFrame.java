package memeograph.renderer.processing;

public class InteractiveMemeoFrame extends MemeoFrame{

    @Override
    public ProcessingApplet getGraphDisplayer(){
        return new InteractiveProcessingApplet();
    }
}
