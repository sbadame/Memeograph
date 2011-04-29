
import java.util.Random;

class Node {
  Node down, right;
  int key;
  String data;

  public Node(int k, String d, Node r, Node dow) {
    key = k;
    data = d;
    right = r;
    down = dow;
  }
}

public class Skiplist {

  Node topright = new Node(10000, "", null, null);
  Node topleft = new Node(-1, "", topright, null);
  Random r = new Random();

  void insert(int key, String data) {
    Node curr = topleft;
    while (key > curr.right.key) {
      curr = curr.right;
    }

    Node nn;
    if ((nn = insertAtNode(curr, key, data)) != null) {
      curr.right = new Node(key, data, curr.right, nn);

      nn = curr.right;
      while (r.nextFloat() < .25) {
        topright = new Node(10000, "", null, topright);
        nn = new Node(key, data, topright, nn);
        topleft = new Node(-1, "", nn, topright);
      }
    }
  }

  Node insertAtNode(Node n, int key, String data) {
    while (key > n.right.key) {
      n = n.right;
    }

    if (n.down == null) {
      Node nn = new Node(key, data, n.right, null);
      n.right = nn;
      if (r.nextFloat() < .25) {
        return nn;
      } else {
        return null;
      }
    } else {
      Node nn = insertAtNode(n.down, key, data);
      if (nn != null) {
        nn = new Node(key, data, n.right, nn);
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
