package memeograph;


public class HeapObject extends DiGraph{

    public HeapObject(String name){
        super(name);
    }

    public void addDataChild(HeapObject child){
        addYChild(child);
    }

    public Iterable<HeapObject> getDataChildren(){
        return getYIterator();
    }
}
