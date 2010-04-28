package memeograph;

public class HeapObject extends DiGraph{

    public HeapObject(){
        super();
    }

    public void setName(String name){
        setData(name);
    }

    public String getName(){
        return getData();
    }

    public void addDataChild(HeapObject child){
        addZChild(child);
    }

    public Iterable<HeapObject> getDataChildren(){
        return getZIterator();
    }

    public void addSoftwareChild(HeapObject ho){
        addYChild(ho);
    }

    public Iterable<HeapObject> getSoftwareChildren(){
        return getYIterator();
    }
  }
