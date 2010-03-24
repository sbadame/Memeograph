public class BST {
    BST left, right;
    int data;

    public BST() {}

    public BST(int data) {
        this.data = data;
        memeographname = "BST(" + data + ")";

        left = new Leaf();
        right = new Leaf();
    }

    public BST insert(int n)
    {
        if (n < data)
            left = left.insert(n);
        else
            right = right.insert(n);

        return this;
    }

    String memeographname = "BST()";
}
