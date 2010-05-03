package memeograph;

import java.awt.Color;
import java.util.*;
import memeograph.ui.MemeoPApplet;


/*
 * The data structure behind it all.
 * This represents a node in our giant graph.
 * Kids can be added in the y or z direction. Subclasses decide how to organize
 * the data. This just stores it all.
 * This is a sort of doubly linked Node. Every DiGraph node has a reference
 * to its parent.
 */
public abstract class DiGraph{

    public static MemeoPApplet listener;

    private String data = null;
    private Color color = null;

    protected Vector<DiGraph> yparents = new Vector<DiGraph>();
    protected Vector<DiGraph> zparents = new Vector<DiGraph>();

    private Vector<DiGraph> yChildren = new Vector<DiGraph>();
    private Vector<DiGraph> zChildren = new Vector<DiGraph>();

    public DiGraph(String data, Iterable<DiGraph> ykids, Iterable<DiGraph> zkids)
    {
        setData(data);
        if (ykids != null)
            for (DiGraph kid : ykids) {
                //Avoid setting off the listeners
                yChildren.add(kid);
            }

        if (zkids !=  null)
            for (DiGraph kid : zkids) {
                zChildren.add(kid);
            }
    }

    public DiGraph(String data) { this(data, null, null); }
    public DiGraph(Iterable<DiGraph> ykids, Iterable<DiGraph> zkids) { this(null, ykids, zkids); }
    public DiGraph(Object o) {
        setData(o);
    }

    public DiGraph(){ }

    @Override
    public String toString()
    {
        return toString(new HashSet<DiGraph>());
    }

    public String toString(Set<DiGraph> seen)
    {
        seen.add(this);
        if (getData() != null && getYChildren().size() == 0)
            return "\"" + getData() + "\"";

        String swkids = "";
        for (DiGraph kid : getYChildren()) {
            if (seen.contains(kid))
                swkids += " " + kid.getData();
            else
                swkids += " " + kid.toString(seen);
        }

        String dkids = "";
        for (DiGraph kid : getZChildren()) {
            if (seen.contains(kid))
                dkids += " " + kid.getData();
            else
                dkids += " " + kid.toString(seen);
        }

        if (getData() == null)
            return "(" + swkids.trim() + ")";
        else
            return "(\"" + getData() + "\"   ( " + swkids.trim() + " ) [ " + dkids.trim() + " ])" ;
    }

    /**
     * @return the data
     */
    public String getData() 
    {
        return data;
    }

    public void setData(Object o){
        //Check if we already have data, if we do, remove it.
        if (!yChildren.isEmpty())
            removeYChildren();

        if (o != null) {
            this.data = o.toString();
        } else {
            this.data = "";
        }
    }

    public void setData(String data){
        this.data = data;
    }

    public String name()
    {
        if (getData() != null)
            return getData();
        else
            return "";
    }

    private static <E> Iterable<E> getIterator(List<E> list){
        final Iterator<E> iterator = list.iterator();
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return new Iterator<E>(){
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    public E next() {
                        return (E)iterator.next();
                    }
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    /**
     * @return the ykids
     */
    public Vector<DiGraph> getYChildren()
    {
        return yChildren;
    }


    public void addYChild(DiGraph ychild)
    {
        ychild.yparents.add(this);
        yChildren.add(ychild);
    }

    public void removeYChildren(){
        for (DiGraph child : yChildren) {
            child.yparents.remove(this);
        }
        yChildren.clear();
    }

    public Vector<DiGraph> getZChildren()
    {
        return zChildren;
    }


    public void addZChild(DiGraph zchild)
    {
        if (zchild == this) {
            throw new IllegalArgumentException("Can't add yourself as a child");
        }

        zchild.zparents.add(this);
        zChildren.add(zchild);
    }

    public Vector<DiGraph> getChildren()
    {
        Vector<DiGraph> v = new Vector<DiGraph>(getYChildren());
        v.addAll(getZChildren());
        return v;
    }

    public List<DiGraph> getDiGraphParents(){
        return yparents;
    }

    public List<DiGraph> getZParents(){
        return zparents;
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

    void removeChildren() {
        yChildren.clear();
        zChildren.clear();
        yparents.remove(this);
        zparents.remove(this);
    }
}
