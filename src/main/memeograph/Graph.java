package memeograph;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.util.HashMap;
import java.util.Map;

public class Graph {
    private HashMap<StackFrame, StackObject> stackMap = new HashMap<StackFrame, StackObject>();
    private HashMap<ThreadReference, ThreadHeader> threads = new HashMap<ThreadReference, ThreadHeader>();
    private HeapObjectFactory hof = new HeapObjectFactory(new String[]{"java", "sun"});

    private SuperHeader supernode = new SuperHeader("Memeographer!");

    public Graph(){}

    public SuperHeader getSuperNode() {
        return supernode;
    }

    public HeapObject getHeapObject(Value val){
        return hof.getHeapObject(val);
    }

    public HashMap<StackFrame, StackObject> getStackMap(){
        return stackMap;
    }

    public HashMap<ThreadReference, ThreadHeader> threads(){
        return threads;
    }

    public Map<Value, HeapObject> getHeapMap(){
        return hof.getHeapMap();
    }
}
