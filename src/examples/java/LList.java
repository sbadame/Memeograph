import java.awt.Color;

public class LList {
    Integer data;
    LList next;
    public LList() {}
    public LList(Integer data, LList next) 
    {
        this.data = data;
        this.next = next;
        memeographname = "LList(" + data + ")";
    }
    public void insert(int data){
       LList l = new LList(data,this.next);
       this.next = l;
    }
    public void pause(){}

    protected int memeographcolor = 0xaf4272;
    protected String memeographname = "LList()";
}
