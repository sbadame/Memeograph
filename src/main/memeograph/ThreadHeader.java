package memeograph;

import com.sun.jdi.ThreadReference;

public class ThreadHeader extends DiGraph{

    public ThreadHeader(ThreadReference t) {
        super(t.name());
    }

    public void setFrame(StackObject so){
        if (getYChildren().isEmpty() == false) {
            throw new IndexOutOfBoundsException("ThreadHeader can only point to 1 StackObject");
        }
        addYChild(so);
    }

    public StackObject getFrame(){
        return (StackObject)getYChildren().firstElement();
    }

    public boolean hasFrame(){
        return !getYChildren().isEmpty();
    }
}