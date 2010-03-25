import java.awt.Color;

public class Leaf extends BST {

    public Leaf() {
        super();
        memeographname = "Leaf()";
    }

    @Override
    public BST insert(int n) {
        return new BST(n);
    }

    Color memeographcolor = Color.GREEN;
}
