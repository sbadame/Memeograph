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

    protected Color memeographcolor = new Color(200, 100, 100);
    protected String memeographname = "LList()";
}
