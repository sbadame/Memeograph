package memeograph.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CompoundIterator<E> implements Iterator<E> {

  private Iterator<E> current;
  private Iterator<Iterator<E>> iterators;

  public CompoundIterator(Collection<Iterator<E>> iterators) {
    this.iterators = iterators.iterator();
    if (this.iterators.hasNext()) { this.current = this.iterators.next(); }
  }

  public CompoundIterator(Iterator<E>... iterators){
    List<Iterator<E>> asList = Arrays.asList(iterators);
    this.iterators = asList.iterator();
    if (this.iterators.hasNext()) { this.current = this.iterators.next(); }
  }

  public boolean hasNext() {
    if (current == null) { return false; }
    if (current.hasNext()) { return true; }

    while (iterators.hasNext()) {
        current = iterators.next();
        if (current.hasNext()) {
          return true;
        }
    }
    return false;
  }

  public E next() {
    if (current == null) { throw new NoSuchElementException(); }
    if (current.hasNext()) {
      return current.next();
    } else {
      if (iterators.hasNext()) {
        current = iterators.next();
        return next();
      } else {
        throw new NoSuchElementException();
      }
    }
  }

  public void remove() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
