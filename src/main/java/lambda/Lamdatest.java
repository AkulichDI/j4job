package lambda;

import java.util.Arrays;
import java.util.Comparator;

public class Lamdatest {

    public static void main(String[] args) {

        String[] names = {
                "Ivan",
                "Dima"
        };

        Comparator<String> lengthComparator = (left, right ) -> {
            System.out.println("execute");
            return Integer.compare(left.length(), right.length());
        };
        Arrays.sort(names,lengthComparator);
        System.out.println(names);

    }
}
