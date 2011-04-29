
import java.util.Random;

class SLNode {
  SLNode down, right;
  int key;
  String data;

  public SLNode(int k, String d, SLNode r, SLNode dow) {
    key = k;
    data = d;
    right = r;
    down = dow;
  }
}

public class Skiplist {

  SLNode topright = new SLNode(10000, "", null, null);
  SLNode topleft = new SLNode(-1, "", topright, null);
  Random r = new Random();

  void insert(int key, String data) {
    SLNode curr = topleft;
    while (key > curr.right.key) {
      curr = curr.right;
    }

    SLNode nn;
    if ((nn = insertAtNode(curr, key, data)) != null) {
      curr.right = new SLNode(key, data, curr.right, nn);

      nn = curr.right;
      while (r.nextFloat() < .25) {
        topright = new SLNode(10000, "", null, topright);
        nn = new SLNode(key, data, topright, nn);
        topleft = new SLNode(-1, "", nn, topright);
      }
    }
  }

  SLNode insertAtNode(SLNode n, int key, String data) {
    while (key > n.right.key) {
      n = n.right;
    }

    if (n.down == null) {
      SLNode nn = new SLNode(key, data, n.right, null);
      n.right = nn;
      if (r.nextFloat() < .25) {
        return nn;
      } else {
        return null;
      }
    } else {
      SLNode nn = insertAtNode(n.down, key, data);
      if (nn != null) {
        nn = new SLNode(key, data, n.right, nn);
        n.right = nn;
        if (r.nextFloat() < 25) {
          return nn;
        }
      }
      return null;
    }
  }

  void pause() {
  }

  public static void main(String arg[]) {
    Skiplist s = new Skiplist();
    for (int i = 0; i < 100; i++) {
      s.insert(i, "" + i);
      s.pause();
    }
    System.out.println("All inserted!");
  }
}
