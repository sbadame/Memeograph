package memeograph.renderer.processing;
import java.util.Iterator;
import memeograph.graph.Node;
import memeograph.renderer.processing.NodeGraphicsInfo;
import memeograph.util.Closure;
import memeograph.util.CompoundIterator;
import memeograph.util.Filter;
import memeograph.util.Transformer;
import memeograph.util.Util;

/**
 * Allows for the creation of many fancy iterators that go through
 * graphs.
 */
class DisplayGraphIterator implements Iterator<NodeGraphicsInfo> {

  protected static Filter<NodeGraphicsInfo> ALL_PASS_FILTER = new Filter<NodeGraphicsInfo>(){
    public Boolean filter(NodeGraphicsInfo from) { return true; }
  };

  protected static Closure<NodeGraphicsInfo> DO_NOTHING = new Closure<NodeGraphicsInfo>(){
    public void execute(NodeGraphicsInfo e) {}
  };

  protected NodeGraphicsInfo head;
  protected Iterator<NodeGraphicsInfo> childIterator;
  protected boolean usedHead = false;
  protected Filter<NodeGraphicsInfo> filter = ALL_PASS_FILTER;
  protected Closure<NodeGraphicsInfo> onNext = DO_NOTHING;

  public DisplayGraphIterator(NodeGraphicsInfo head){
    this.head = head;
  }

  public DisplayGraphIterator(NodeGraphicsInfo head, Filter<NodeGraphicsInfo> filter){
    this(head);
    this.filter = filter;
    if (this.filter.filter(head) == false) { usedHead = true; }
  }

  public DisplayGraphIterator(NodeGraphicsInfo head, Filter<NodeGraphicsInfo> filter, Closure<NodeGraphicsInfo> onNext){
    this(head, filter);
    this.onNext = onNext;
  }

  public boolean hasNext() {
    if (head == null) {return false;}
    if (!usedHead) { return true; }
    if (childIterator == null) { setChildIterator(); }
    return childIterator.hasNext();
  }

  public NodeGraphicsInfo next() {
    if (!usedHead) {
      usedHead = true;
      onNext.execute(head);
      return head;
    }
    if (childIterator == null) {
      setChildIterator();
    }
    NodeGraphicsInfo next = childIterator.next();
    onNext.execute(next);
    return next;
  }

  private void setChildIterator(){
      if (head == null) { throw new NullPointerException(); }
      childIterator = new CompoundIterator<NodeGraphicsInfo>(
        Util.map(head.getChildren(), new Transformer<NodeGraphicsInfo, Iterator<NodeGraphicsInfo>>() {
            @Override
            public Iterator<NodeGraphicsInfo> transform(NodeGraphicsInfo from) {
              return new DisplayGraphIterator(from, filter, onNext);
            }
        }
      ));

  }

  public void remove() { throw new UnsupportedOperationException(); }
}
