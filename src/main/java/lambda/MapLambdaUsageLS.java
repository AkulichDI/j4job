package lambda;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MapLambdaUsageLS {
   /* public static void main(String[] args) {

        Map<Integer, String> map = new HashMap<>();
        map.put(1, "name");
        map.put(2, "top");
        map.put(3, "user");
        map.put(4, "precision");
        map.put(5, "post");

        BiFunction<Integer, String, String> function = (key, value) -> value + "_" + key;
        map.replaceAll(function);
        map.forEach((key, value) -> System.out.println("Key: " + key + ", value: " + value));
    }*/


/*
    public static void main(String[] args) {

        Map<Integer, String> map = new HashMap<>();
        map.put(1, "name");

        BiFunction<Integer, String, String> function = (key, value) -> value + "_" + key;
        String result = map.computeIfPresent(1, function);

        System.out.println("Current value: " + result);
        map.forEach((key, value) -> System.out.println("Key: " + key + ", value: " + value));
    }

 */




    public static void main(String[] args) {

        Map<String, Integer> map = new HashMap<>();

        Function<String, Integer> function = String::length;
        System.out.println("Result: " + map.computeIfAbsent("Petr", function));
        System.out.println("Result: " + map.computeIfAbsent("Petr", key -> key.length() + 10));
        map.forEach((key, value) -> System.out.println("Key: " + key + ", value: " + value));
    }
}
