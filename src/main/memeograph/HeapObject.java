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

    public void addSoftwareChild(HeapObject ho){
        addYChild(ho);
    }
  }
