import java.awt.Color;

public class Leaf extends BST {


    public Leaf() {
        memeographname = "Leaf()";
        memeographcolor = 0x00FF00;
    }

    @Override
    public BST insert(int n) {
        memeopoint = true;
        return new BST(n);
    }

}
