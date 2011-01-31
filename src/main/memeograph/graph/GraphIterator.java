package memeograph.graph;

import java.util.Iterator;
import memeograph.util.Closure;
import memeograph.util.CompoundIterator;
import memeograph.util.Filter;
import memeograph.util.Transformer;
import memeograph.util.Util;

/**
 * Allows for the creation of many fancy iterators that go through
 * graphs.
 */
class GraphIterator implements Iterator<Node> {

  private static Filter<Node> ALL_PASS_FILTER = new Filter<Node>(){
    public Boolean filter(Node from) { return true; }
  };

  private static Closure<Node> DO_NOTHING = new Closure<Node>(){
    public void execute(Node e) {}
  };

  private final Node head;
  private Iterator<Node> childIterator;
  private boolean usedHead = false;
  private Filter<Node> filter = ALL_PASS_FILTER;
  private Closure<Node> onNext = DO_NOTHING;

  public GraphIterator(Node head){
    this.head = head;
  }

  public GraphIterator(Node head, Filter<Node> filter){
    this(head);
    this.filter = filter;
    if (this.filter.filter(head) == false) { usedHead = true; }
  }

  public GraphIterator(Node head, Filter<Node> filter, Closure<Node> onNext){
    this(head, filter);
    this.onNext = onNext;
  }

  public boolean hasNext() {
    if (head == null) {return false;}
    if (!usedHead) { return true; }
    if (childIterator == null) { setChildIterator(); }
    return childIterator.hasNext();
  }

  public Node next() {
    if (!usedHead) {
      usedHead = true;
      onNext.execute(head);
      return head;
    }
    if (childIterator == null) {
      setChildIterator();
    }
    Node next = childIterator.next();
    onNext.execute(next);
    return next;
  }

  private void setChildIterator(){
      if (head == null) { throw new NullPointerException(); }
      childIterator = new CompoundIterator<Node>(
        Util.map(head.getChildren(), new Transformer<Node, Iterator<Node>>() {
            @Override
            public Iterator<Node> transform(Node from) {
              return new GraphIterator(from, filter, onNext);
            }
        }
      ));

  }

  public void remove() { throw new UnsupportedOperationException(); }
}
