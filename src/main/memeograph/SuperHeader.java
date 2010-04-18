package memeograph;

public class SuperHeader extends DiGraph{

    public SuperHeader(String name){
        super(name);
    }

    public Iterable<ThreadHeader> getThreads(){
        return getYIterator();
    }

    public void addThread(ThreadHeader header){
        addYChild(header);
    }

}
