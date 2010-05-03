package memeograph;

public class SuperHeader extends DiGraph{

    public SuperHeader(String name){
        super(name);
    }

    public Iterable<DiGraph> getThreads(){
        return getYChildren();
    }

    public void addThread(ThreadHeader header){
        addYChild(header);
    }

}
