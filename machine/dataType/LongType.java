package dataType;

/**
 * @version : 1.0
 * @author: momoshenchi
 * @date: 2021/6/14 - 19:18
 */
public class LongType extends  BaseType
{
    private long value;


    public LongType(){
        this.value = 0L;
    }

    public LongType(long value){
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
