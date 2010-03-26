package memeograph;

public interface TreeChangeListener {
    public void kidAdded(DiGraph parent, DiGraph addedNode);
    public void childrenRemoved(DiGraph parent);
    public void dataChanged(DiGraph parent);
}
