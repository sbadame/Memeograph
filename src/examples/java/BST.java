
public class BST {
    BST left, right;
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
            left = left.insert(n);
            return this;
        }else{
            right = right.insert(n);
            return this;
        }
    }

    protected int memeographcolor = 0xCDB79E;
    protected String memeographname = "BST()";

    @Override
    public String toString()
    {
        return "(" + left + " " + data + " " + right + ")";
    }
}
