package memeograph.renderer.processing;

public class InteractiveMemeoFrame extends MemeoFrame{

    @Override
    public ProcessingApplet getApplet(){
        return new InteractiveProcessingApplet();
    }
}
