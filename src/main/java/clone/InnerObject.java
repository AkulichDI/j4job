package clone;

public class InnerObject implements Cloneable {

    int num;

    @Override
    public InnerObject clone() throws CloneNotSupportedException {

        return (InnerObject)super.clone();


    }




}
