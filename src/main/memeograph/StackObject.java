package memeograph;

public class StackObject extends DiGraph{

    public StackObject(String name){
        super(name);
    }

    public void setNextFrame(StackObject so){
        if (getYChildren().size() != 0) {
            throw new RuntimeException("A StackObject can only point to 1 frame");
        }
        addYChild(so);
    }

    public StackObject nextFrame(){
        DiGraph nextframe = getYChildren().firstElement();
        if (nextframe instanceof StackObject) {
            return (StackObject)nextframe;
        }else{
            throw new ClassCastException("Next object cannot be casted into a StackObject");
        }
    }

    public void removeNextFrame(){
        getYChildren().clear();
    }

    public boolean hasNextFrame(){
        return !getYChildren().isEmpty();
    }
}
