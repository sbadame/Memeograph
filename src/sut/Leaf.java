import java.awt.Color;

public class Leaf extends BST {


    public Leaf() {
        memeographname = "Leaf()";
        memeographcolor = Color.GREEN;
    }

    @Override
    public BST insert(int n) {
        memeopoint = true;
        return new BST(n);
    }

}
