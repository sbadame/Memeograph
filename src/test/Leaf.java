import java.awt.Color;

public class Leaf extends BST {


    public Leaf() {
        super();
        memeographname = "Leaf()";
        memeographcolor = Color.GREEN;
    }

    @Override
    public BST insert(int n) {
        return new BST(n);
    }

}
