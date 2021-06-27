package dataType;

public class DoubleType extends BaseType
{
    private double value;

    public DoubleType(){
        value = 0.0;
    }

    public DoubleType(double value){
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }


}
