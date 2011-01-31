package memeograph.util;

public interface Transformer<A, B> {
  public abstract B transform(A from);
}
