package memeograph;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.HashMap;

public class GraphBuilder {

	private VirtualMachine vm;
	private Tree graph =  new Tree("Memeograph!");
  private HashMap<String, Tree> treeMap = new HashMap<String, Tree>();

	public GraphBuilder(VirtualMachine vm){
		this.vm = vm;
	}

	public void buildGraph(){
		vm.suspend();

		//First we go through all of the loaded classes
		//Do we really need to do this?
		for(ReferenceType c : vm.allClasses()){
				searchClass(c);
		}

		//Now we go through all of the threads
		for (ThreadReference t : vm.allThreads()) {
				getTree(t);
		}

		vm.resume();
	}

	private void searchClass(ReferenceType t){
		for (ObjectReference o : t.instances(0)) {
				exploreObject(o);
		}
	}

		/**
		* The String representation of an Object. NOTE: This needs to be unique
		* for every object. Otherwise building the graph will probably go wrong.
		*/
		protected String getText(ObjectReference or){
				return or.referenceType().name() + "<" + or.uniqueID() + ">";
		}

		/**
		* The String representation of a Stack Frame. NOTE: This needs to be unique
		* for every object. Otherwise building the graph will probably go wrong.
		*/
		protected String StackFrame2String(int depth){
				return "StackFrame" + depth;
		}


		/**
		 * Traverses the frames of this thread until it reaches local 
		 * variables
		 */
		private void getTree(ThreadReference t) {
				//t.frame[0] == current frame
				//t.frame[t.frameCount() - 1] == top most frame
				try {
						int i = 0;
						for (StackFrame frame: t.frames()) {
								Tree tree = exploreStackFrame(frame, i);

							  //Now where to put this tree...?
								if (i ==  0){ //The top most frame
										graph.addChild(tree);
								}else{ //Just add to the previous Stack Frame Tree
										System.out.println((i-1) + " -> " +  i );
										getStackFrame(i-1).addChild(tree);
								}
								i++;
						}
				} catch (IncompatibleThreadStateException itse) {
						System.err.println("Why in the world do we have an IncompatibleThreadStateException?");
						itse.printStackTrace();
				}
		}

		private Tree getStackFrame(int depth){
				String key = StackFrame2String(depth);
				if (!treeMap.containsKey(key)){
						treeMap.put(key, new Tree(key));
				}
				return treeMap.get(key);
		}

	
		private Tree exploreStackFrame(StackFrame frame, int depth){
				Tree tree = getStackFrame(depth);
				try {
						for (Value val : frame.getValues(frame.visibleVariables()).values()) {
								if (val != null && val.type() != null && val.type() instanceof ClassType)
										tree.addChild(exploreObject((ObjectReference)val));
						}
				} catch (AbsentInformationException ex) {
						//Seems to only be thrown when we see a frame with no variables
						//that we can access, not sure if this something need be looked into
						//System.err.println("AbsentInformaionException at " + StackFrame2String(depth));
						//System.err.println(ex);
				}
				return tree;
		}

		protected boolean filterObject(ObjectReference o){
				if (o.referenceType().name().startsWith("java.")) return false;
				if (o.referenceType().name().startsWith("sun.")) return false;
				return true;
		}

		private Tree exploreObject(ObjectReference or){
				String txt = getText(or);
				if (treeMap.containsKey(txt)){
						return treeMap.get(txt);
				}

				Tree tree = new Tree();
				tree.setData(txt);
				treeMap.put(txt, tree); //Do this right off the bat to prevent infinite loop
																//With cycling graphs
				if ( filterObject(or) ){
						for (Value val : or.getValues(or.referenceType().allFields()).values() ) {
								if ( val != null && val.type() != null && val.type() instanceof ClassType ){
									 ObjectReference child = (ObjectReference)	val;
									 tree.addChild(exploreObject(child));
								}
						}
				}
				return treeMap.get(txt);
		}

		public Tree getGraph(){
				return graph;
		}

		public HashMap<String, Tree> getGraphMap(){
				return treeMap;
		}

}
