package lambda;

import java.lang.classfile.CompoundElement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LambdaUsage {

    public static void main(String[] args) {
        List<String> strings = Arrays.asList("eee", "a", "ccc", "ddd", "bb" );
        Comparator<String> comparator = (left, right) -> {
            System.out.println("left: " + left.length() + " right: " + right.length());
            return Integer.compare(left.length(), right.length());
        };
        strings.sort(comparator);
        for(String string : strings){
            System.out.println(string);
        }
    }



}
