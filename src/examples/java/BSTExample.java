
public class BSTExample{
    public static void main(String[] args){
        LList l = new LList();
        l.pause();
        BST b = new BST(5);
        l.pause();
        b = b.insert(10);
        l.pause();
        b = b.insert(15);
        b = b.insert(12);
        l.pause();
        b = b.insert(2);
        l.pause();
        b = b.insert(1);
        l.pause();
        
        l.insert(-3);
        l.pause();
        l.insert(6);
        l.pause();
        l.insert(7);
        l.pause();
        l.insert(8);
        l.pause();
        
        b = b.insert(5);
    }

}
