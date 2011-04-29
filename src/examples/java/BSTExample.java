import java.util.LinkedList;
public class BSTExample{
    public static void main(String[] args){
        LList l = new LList();
        BST b = new BST(5);
        b = b.insert(10);
        l.insert(-3);
        b = b.insert(15);
        l.insert(6);
        b = b.insert(12);
        b = b.insert(2);
        l.insert(8);
        b = b.insert(1);
        l.insert(7);
        b = b.insert(5);
    }

}
