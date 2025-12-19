package streamApi;

import java.util.Arrays;
import java.util.List;

public class CountExample {

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9,0);
        long sum = numbers.stream()
                .filter(element -> element % 2 == 0 )
                .count();
        System.out.println(sum);
    }
}
