
class Node<Key extends Comparable<Key>, Value> {
        public Key key;
        public Value val;
        public Node<Key,Value> left, right;
        private boolean color;
        private int memeographcolor;
        public void flipColor()
        {
          setColor(!getColor());
        }

        public boolean getColor() { return color; }
        public void setColor(boolean col)
        {
          color = col;
          memeographcolor = color ? 0xAA1111 : 0x111111;
        }

        Node(Key key, Value val) {
            this.key = key;
            this.val = val;
            setColor(LLRB.RED);
        }
    }

public class LLRB<Key extends Comparable<Key>, Value>
{
    public static final boolean RED    = true;
    public static final boolean BLACK = false;
    private Node<Key, Value> root;
   

    public Value search(Key key) {
        Node<Key, Value> x = root;
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if (cmp == 0) return x.val;
            else if (cmp < 0) x = x.left;
            else if (cmp > 0) x = x.right;
        }        
        return null;
    }    

    public void insert(Key key, Value value)
    {    
         root = insert(root, key, value);
         root.setColor(BLACK);
    }    

    private Node<Key, Value> insert(Node<Key, Value> h, Key key, Value value)
    {    
         if (h == null)        return new Node<Key, Value>(key, value);
         if (isRed(h.left) && isRed(h.right)) colorFlip(h);
         int cmp = key.compareTo(h.key);
         if (cmp == 0)      h.val = value;
         else if (cmp < 0) h.left =  insert(h.left, key, value);
         else                  h.right = insert(h.right, key, value);
         if (isRed(h.right) && !isRed(h.left))     h = rotateLeft(h);
         if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
         return h;
    }

    Node<Key, Value> rotateLeft(Node<Key, Value> h) {
        Node<Key, Value> x = h.right;
        h.right = x.left;
        x.left = h;
        x.setColor(h.getColor());
        h.setColor(RED);
        return x;
    }

    
Node rotateRight(Node h)
{
   Node x = h.left;
   h.left= x.right;
   x.right= h;
   x.setColor(h.getColor());
   h.setColor(RED);
   return x;
}

void colorFlip(Node h)
{
   h.flipColor();
   h.left.flipColor();
   h.right.flipColor();
}

boolean isRed(Node n) 
{
    if (n == null) return false;
    return n.getColor() == RED;
}

void pause() {}


    public static void main(String args[]) {
        LLRB<Integer, String> rbtree = new LLRB<Integer, String>();
        for (int i = 0; i < 100; i++) {
                rbtree.insert(i, "" + i);
                System.out.println(i);
                rbtree.pause();
        }
        rbtree.pause();
    }
}
