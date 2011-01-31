package memeograph.util;

import java.util.ArrayList;
import java.util.Collection;

public class Util {

  static public <C,D> Collection<D> map(Collection<C> c, Transformer<C, D> t){
    ArrayList<D> arrayList = new ArrayList<D>(c.size());
    for (C member : c) {
      arrayList.add(t.transform(member));
    }
    return arrayList;
  }

  static public <C> Collection<C> filter(Collection<C> c, Transformer<C, Boolean>t){
    ArrayList<C> arrayList = new ArrayList<C>();
    for (C object : c) {
      if (t.transform(object)) {
        arrayList.add(object);
      }
    }
    return arrayList;
  }

  //Returns a new stringbuffer with the whitespace trimmed off the edges
  static public StringBuilder trim(CharSequence s){
    StringBuilder b = new StringBuilder(s);

    while(b.length() > 0 && b.charAt(0) == ' '){
      b.deleteCharAt(0); //Remove the space from the start
    }

    while(b.length() > 0 && b.charAt(b.length()-1) == ' '){
      b.deleteCharAt(b.length()-1); //Remove the space from the end
    }

    return b;
  }

}
