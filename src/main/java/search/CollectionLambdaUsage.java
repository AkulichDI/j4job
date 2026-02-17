package search;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class CollectionLambdaUsage {
    public static void main(String[] args) {
        Collection<String> collection = new ArrayList<>();
        collection.add("name");
        collection.add("top");
        collection.add("user");
        collection.add("precision");
        collection.add("post");

        Predicate<String> predicate = s -> s.length() == 4;
        collection.removeIf(predicate);

        collection.forEach(System.out::println);
    }
}

class ListLambdaUsage {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(2, 4, 7, 9, 12);
        UnaryOperator<Integer> operator = i -> i * i;
        list.replaceAll(operator);
        list.forEach(s -> System.out.println("Current number: " + s));
    }
}