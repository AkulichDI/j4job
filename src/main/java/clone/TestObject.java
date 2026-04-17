package clone;

public class TestObject implements Cloneable{

    int num;
    InnerObject innerObject;

    @Override
    protected TestObject clone() throws CloneNotSupportedException {
        TestObject testObject =  (TestObject)super.clone();
        testObject.innerObject = innerObject.clone();
        return testObject;
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        TestObject testObj1 = new TestObject();

        testObj1.num = 5;

        InnerObject innerObj = new InnerObject();

        innerObj.num = 15;

        testObj1.innerObject = innerObj;

        TestObject testObj2 = testObj1.clone();

        testObj2.num = 25;

        testObj2.innerObject.num = 35;

        System.out.println("Исходный класс: " + testObj1.num);

        System.out.println("Исходный класс: " + testObj1.innerObject.num);

        System.out.println("Скопированный класс: " + testObj2.num);

        System.out.println("Скопированный класс: " + testObj2.innerObject.num);


    }

}
