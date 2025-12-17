package lambda;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class RefMethod {

    public static void cutOut(String value){
        if(value.length() > 10 ){
            System.out.println(value.substring(0,10) + "...");
        }else {
            System.out.println(value);
        }
    }



    public static void main(String[] args) {

        List<String> names = Arrays.asList(
                "Ivan",
                "Dima"
        );

        Consumer<String> out = RefMethod::cutOut;
        names.forEach(out);



       /*
        Comparator<Integer> integerComparator = (left , right) -> Integer.compare(left,right);
        Comparator<Integer> intComp = Integer::compareTo;
        names.sort(String::compareTo);


        Consumer<String> consumer = (name) -> System.out.println(name);
        Consumer<String> consu = System.out::println;
        names.forEach(consu);

        */

    }


}
