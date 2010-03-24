import java.awt.Color;

public class Leaf extends BST {
    public Leaf() {}

    @Override
    public BST insert(int n) {
        return new BST(n);
    }

    String memeographname = "Leaf()";
    Color memeographcolor = Color.GREEN;
}
