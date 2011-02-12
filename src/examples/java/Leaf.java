public class Leaf extends BST {


    public Leaf() {
        memeographname = "Leaf()";
        memeographcolor = 0x00FF00;
    }

    @Override
    public BST insert(int n) {
        return new BST(n);
    }

}
