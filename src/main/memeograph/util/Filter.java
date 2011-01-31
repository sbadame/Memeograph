package memeograph.util;

public abstract class Filter<A> implements Transformer<A, Boolean>{
  public Boolean transform(A from){return filter(from);}
  public abstract Boolean filter(A from);
}
