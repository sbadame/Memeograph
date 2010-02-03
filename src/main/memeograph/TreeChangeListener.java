package memeograph;

public interface TreeChangeListener {
    public void kidAdded(Tree parent, Tree addedNode);
    public void childrenRemoved(Tree parent);
    public void dataChanged(Tree parent);
}
