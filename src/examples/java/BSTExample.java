
public class BSTExample{
    public static void main(String[] args){
        System.out.println("BSTEXAMPLE SAYS HELLO");
        BST b = new BST(5);
        b = b.insert(10);
        b = b.insert(15);
        b = b.insert(12);
        b = b.insert(2);
        b = b.insert(1);
        BST b2 = new BST(900);
        b2.insert(500);
        System.out.println("" + b + b2);
        b2.insert(500);

    }

}
