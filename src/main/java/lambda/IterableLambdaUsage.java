package lambda;

import java.util.Arrays;

public class IterableLambdaUsage {

    public static void main(String[] args) {
        Iterable<String> iterable = Arrays.asList(
                "name",
                "top",
                "user",
                "precision",
                "post"
        );
        iterable.forEach(System.out::println);
    }

}
