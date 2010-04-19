package memeograph;

import com.sun.jdi.Value;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import java.awt.Color;


public abstract class SpecialField {

    public static SpecialField[] SPECIAL_CASES = new SpecialField[]{new ColorCase(), new NameCase()};

    public static SpecialField getSpecialField(Field f, Value v){
        for (SpecialField specialFieldCase : SPECIAL_CASES) {
            if (specialFieldCase.isSpecialCase(f, v)) {
                return specialFieldCase;
            }
        }
        return null;
    }

    public abstract boolean isSpecialCase(Field f, Value v);
    public abstract void apply(HeapObject ho, Field f, Value v);
}

class ColorCase extends SpecialField{

    @Override
    public boolean isSpecialCase(Field f, Value v) {
        return f.name().equals("memeographcolor") && f.typeName().equals("java.awt.Color");
    }

    @Override
    public void apply(HeapObject ho, Field f, Value v) {
        if (v == null) {System.out.println("\tvalue is null"); return; } //Got this Nullpointer some how...
        ObjectReference colorref = (ObjectReference)v;
        if (colorref == null) {return;}
        Value color_value = colorref.getValue(colorref.referenceType().fieldByName("value"));
        IntegerValue iv = (IntegerValue)color_value;
        ho.setColor(new Color(iv.intValue()));
    }


}

class NameCase extends SpecialField{

    @Override
    public boolean isSpecialCase(Field f, Value v) {
       return f.name().equals("memeographname") && f.typeName().equals("java.lang.String");
    }

    @Override
    public void apply(HeapObject ho, Field f, Value v) {
       if (v == null) {
          return;
       }
       String txt = v.toString();
       txt = txt.substring(1, txt.length()-1);
       ho.setData(txt);
    }

}
