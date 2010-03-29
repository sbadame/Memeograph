import java.util.Scanner;

public class ReadChar{
    public static void main(String[] args){
        BST b = new BST(5);
        b = b.insert(10);
        b = b.insert(15);
        b = b.insert(12);
        b = b.insert(2);
        b = b.insert(1);

        LList l = new LList();
        l = new LList(5, l);
        l = new LList(6, l);
        l = new LList(7, l);
        l = new LList(8, l);

        ReadChar r = new ReadChar();
        System.out.println(r);
        Scanner s = new Scanner(System.in);
        System.out.println(s.nextInt());
        System.out.println(r);
    }

}
