package memeograph;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.util.HashMap;

public class Graph {
    private HashMap<StackFrame, StackObject> stackMap = new HashMap<StackFrame, StackObject>();
    private HashMap<ThreadReference, ThreadHeader> stacks = new HashMap<ThreadReference, ThreadHeader>();

    //Interrogate code
    private SuperHeader supernode = new SuperHeader("Memeographer!");
    private HeapObjectFactory hof = new HeapObjectFactory(new String[]{"java", "sun"});

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
}
