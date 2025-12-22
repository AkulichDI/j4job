package streamApi;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PipelineOutput {

    public static void main(String[] args) {
      /*  Stream.of("one", "two", "three")
                .filter(x -> {
                    System.out.print(x);
                    return x.length() <= 3;
                })
                .map(x -> {
                    System.out.println(x);
                    return x.toUpperCase();
                })
                .forEach(System.out::println);
    }*/

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> numbers1 = Arrays.asList(1, 2, 3, 4, 5);
        Optional<Integer> found = numbers1.stream()
                .filter(n -> n > 5)
                .findFirst();
        found.ifPresent(n -> System.out.println("Found: " + n));
        System.out.println(found);
/*
        Stream.of("one", "one", "two", "three")
              .peek(System.out::println)
                .distinct();
        String stream = Stream.of("one", "two", "three")
                .peek(System.out::println)
                .sorted().toString();
 */



    }
}