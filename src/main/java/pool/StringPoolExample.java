package pool;

public class StringPoolExample {

    public static void main(String[] args) {
/*
        String string1 = "Hello";
        String string2 = new String("Hello");
        String string3 = string2.intern();
        System.out.println(string1 == string3);
*/


        System.out.println(new String("New string") == new String("New string"));

        System.out.println(new String("Interned string").intern() == new String("Interned string").intern());



    }



}
