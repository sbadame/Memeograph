
public class BST {
    final BST left, right;
    final int data;

    private BST(int data, BST left, BST right) {
        this.data = data;
        this.left = left;
        this.right = right;

        memeographname = "BST(" + data + ")";
    }

    public BST() {
        data = 0;
        left = right = null;
    }

    public BST(int data) {
        this.data = data;        
        left = new Leaf();
        right = new Leaf();

        memeographname = "BST(" + data + ")";
    }

    public BST insert(int n)
    {
        if (n < data){
            return new BST(data, left.insert(n), right);
        }else{
            return new BST(data, left, right.insert(n));
        }
    }

    protected int memeographcolor = 0xCDB79E;
    protected String memeographname = "BST()";
}
