package memeograph;

import java.awt.Color;
import java.util.*;

/*
 * The data structure behind it all.
 */
public class DiGraph {

    private String data = null;
    private Vector<DiGraph> children = new Vector<DiGraph>();
    private Color color = null;

    Vector<TreeChangeListener> listeners = new Vector<TreeChangeListener>();

    public DiGraph(String data, Iterable<DiGraph> kids)
    {
        setData(data, false);
        if (kids != null) 
            for (DiGraph kid : kids) {
                //Avoid setting off the listeners
                children.add(kid);
            }
    }

    public DiGraph(String data) { this(data, null); }
    public DiGraph(Iterable<DiGraph> kids) { this(null, kids); }
    public DiGraph(Object o) {
        setData(o, false);
    }

    public DiGraph(){ }

    @Override
    public String toString()
    {
        if (getData() != null && getChildren().size() == 0)
            return getData();

        String rv = "";
        for (DiGraph kid : getChildren()) {
            rv += " " + kid;
        }

        if (getData() == null)
            return "(" + rv.trim() + ")";
        else
            return "(" + getData() + " " + rv.trim() + ")";
    }

    /**
     * @return the data
     */
    public String getData() 
    {
        return data;
    }

    public void setData(Object o){
         setData(o, true);
    }

    private void setData(Object o, boolean fireListener){
         //Check if we already have data, if we do, remove it.
        if (!children.isEmpty())
            removeChildren();
/*
        if (o != null)
            System.out.println(o.getClass().getName() + " " + o);
        else
            System.out.println("null"); */

        if (o != null) {
            this.data = o.toString();
        } else {
            this.data = "";
        }
        if (fireListener)
            fireDataChangedEvent();
    }

    public void setData(String data){
        this.data = data;
    }

    public String getTreeName()
    {
        if (getData() != null)
            return getData();
        else
            return "";
    }

    /**
     * @return the kids
     */
    public Vector<DiGraph> getChildren()
    {
        return children;
    }

    public void addChild(DiGraph child)
    {
        children.add(child);
        fireTreeAddedEvent(child);
    }

    public void removeChildren(){
        children.clear();
        fireChildrenClearedEvent();
    }


    public int depth()
    {
        int depth = 1;
        if (children != null) {
            for (DiGraph kid : children) {
                int kd = kid.depth();
                if (1 + kd > depth) {
                    depth = 1 + kd;
                }
            }
        }

        return depth;
    }

    public void addTreeChangeListener(TreeChangeListener listener){
        listeners.add(listener);
    }

    public void removeTreeChangeListener(TreeChangeListener listener){
        listeners.remove(listener);
    }

    private void fireTreeAddedEvent(DiGraph child){
        for (TreeChangeListener nodeChangeListener : listeners) {
            nodeChangeListener.kidAdded(this, child);
        }
    }

    private void fireChildrenClearedEvent(){
        for (TreeChangeListener nodeChangeListener : listeners) {
            nodeChangeListener.childrenRemoved(this);
        }
    }

    private void fireDataChangedEvent() {
        for (TreeChangeListener nodeChangeListener : listeners) {
            nodeChangeListener.dataChanged(this);
        }
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }
}
