import java.util.Scanner;

public class ReadChar{
    public static void main(String[] args){
        BST b = new BST(5);
        b = b.insert(10);
        b = b.insert(15);
        b = b.insert(12);
        b = b.insert(2);
        b = b.insert(1);
        ReadChar r = new ReadChar();
        System.out.println(r);
        Scanner s = new Scanner(System.in);
        System.out.println(s.nextInt());
        System.out.println(r);
    }

}
