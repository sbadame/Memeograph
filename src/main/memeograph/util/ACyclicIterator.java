package memeograph.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ACyclicIterator<E> implements Iterator<E>{

  private final Iterator<E> iterator;
  private final HashSet<E> seen = new HashSet<E>();

  private E next = null;

  public ACyclicIterator(Iterator<E> iterator){
    this.iterator = iterator;
  }

  public boolean hasNext() {
    if (next != null) { return true; }

    //Now this can get expensive.
    //We need to count the number of times that we go through cyclic iterator
    //and compare that to the number of elements in seen. If seen has N elements
    //and we do N+1, then we have a cycle. (I need to really think this through)
    int size = seen.size();
    while (iterator.hasNext()) {
      next = iterator.next();
      if (!seen.contains(next)) {
        return true;
      }else{
        size--;
        if(size < 0) return false;
      }
    }

    return false;
  }

  public E next() {
    if (next != null) {
      seen.add(next);
      E tmp = next;
      next = null;
      return tmp;
    }

    while(iterator.hasNext()){
      E tmp = iterator.next();
      if (!seen.contains(tmp)) {
        seen.add(tmp);
        return tmp;
      }
    }

    throw new NoSuchElementException();
  }

  public void remove() {
    iterator.remove();
  }

}
