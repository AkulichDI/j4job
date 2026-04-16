package memory;

public class Example {

    public static void main(String[] args) {

        int x = 0;
        Object obj = new Object();
        Example example = new Example();

        example.action(obj);


    }


    public void action (Object parameter){

        String str  = parameter.toString();
        System.out.println(str);


    }



}
