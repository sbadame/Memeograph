package memeograph;

import java.awt.Color;
import java.util.*;
import memeograph.ui.MemeoPApplet;


/*
 * The data structure behind it all.
 */
public class DiGraph {

    public static MemeoPApplet listener;

    private String data = null;
    private Color color = null;

    private Vector<DiGraph> softwareparents = new Vector<DiGraph>();
    private Vector<DiGraph> dataparents = new Vector<DiGraph>();

    private Vector<DiGraph> softwarechildren = new Vector<DiGraph>();
    private Vector<DiGraph> datachildren = new Vector<DiGraph>();

    Vector<TreeChangeListener> listeners = new Vector<TreeChangeListener>();

    public DiGraph(String data, Iterable<DiGraph> softwarekids, Iterable<DiGraph> datakids)
    {
        setData(data, false);
        if (softwarekids != null)
            for (DiGraph kid : softwarekids) {
                //Avoid setting off the listeners
                softwarechildren.add(kid);
            }

        if (datakids !=  null)
            for (DiGraph kid : datakids) {
                datachildren.add(kid);
            }
    }

    public DiGraph(String data) { this(data, null, null); }
    public DiGraph(Iterable<DiGraph> swkids, Iterable<DiGraph> datakids) { this(null, swkids, datakids); }
    public DiGraph(Object o) {
        setData(o, false);
    }

    public DiGraph(){ }

    @Override
    public String toString()
    {
        if (getData() != null && getSoftwareChildren().size() == 0)
            return getData();

        String swkids = "";
        for (DiGraph kid : getSoftwareChildren()) {
            swkids += " " + kid;
        }

        String dkids = "";
        for (DiGraph kid : getDataChildren()) {
            dkids += " " + kid;
        }

        if (getData() == null)
            return "(" + swkids.trim() + ")";
        else
            return "(" + getData() + " -  ( " + swkids.trim() + " ) [ " + dkids.trim() + " ])" ;
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
        if (!softwarechildren.isEmpty())
            removeSoftwareChildren();

        if (o != null) {
            this.data = o.toString();
        } else {
            this.data = "";
        }
        if (fireListener)
            fireDataChangedEvent();
        listener.change();
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
     * @return the softwarekids
     */
    public Vector<DiGraph> getSoftwareChildren()
    {
        return softwarechildren;
    }

    public void addSoftwareChild(DiGraph softwarechild)
    {
        if (softwarechild == this) {
            throw new IllegalArgumentException("Can't add yourself as a child");
        }
        softwarechild.softwareparents.add(this);
        softwarechildren.add(softwarechild);
        fireTreeAddedEvent(softwarechild);
        listener.change();
    }

    public void removeSoftwareChildren(){
        for (DiGraph child : softwarechildren) {
            child.softwareparents.remove(this);
        }
        softwarechildren.clear();
        fireChildrenClearedEvent();
        listener.change();
    }

    public Vector<DiGraph> getDataChildren()
    {
        return datachildren;
    }


    public void addDataChild(DiGraph datachild)
    {
        if (datachild == this) {
            throw new IllegalArgumentException("Can't add yourself as a child");
        }

        datachild.dataparents.add(this);
        datachildren.add(datachild);
        fireTreeAddedEvent(datachild);
        listener.change();
    }

    public Vector<DiGraph> getChildren()
    {
        Vector<DiGraph> v = new Vector<DiGraph>(getSoftwareChildren());
        v.addAll(getDataChildren());
        return v;
    }

    public List<DiGraph> getSoftwareParents(){
        return softwareparents;
    }

    public List<DiGraph> getDataParents(){
        return dataparents;
    }


    /*
    public int depth()
    {
        int depth = 1;
        if (softwarechildren != null) {
            for (DiGraph kid : softwarechildren) {
                int kd = kid.depth();
                if (1 + kd > depth) {
                    depth = 1 + kd;
                }
            }
        }

        return depth;
    }*/

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
