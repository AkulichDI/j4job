package lambda;

import java.util.Date;
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



/*
    public static void main(String[] args) {

        Map<String, Integer> map = new HashMap<>();

        Function<String, Integer> function = String::length;
        System.out.println("Result: " + map.computeIfAbsent("Petr", function));
        System.out.println("Result: " + map.computeIfAbsent("Petr", key -> key.length() + 10));
        map.forEach((key, value) -> System.out.println("Key: " + key + ", value: " + value));
    }*/


/*
    public static void main(String[] args) {

        Map<String, Integer> map = new HashMap<>();
        map.put("Shoes", 200);
        BiFunction<Integer, Integer, Integer> function = (oldValue, newValue) -> oldValue - newValue;
        int newPrice = map.merge("Shoes", 50, function);
        System.out.println("New price: " + newPrice);
        map.forEach((key, value) -> System.out.println("Key: " + key + ", value: " + value));
    }*/


/*
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        map.put("Shoes", 200);
        BiFunction<Integer, Integer, Integer> function = (oldValue, newValue) -> oldValue - newValue;
        int newPrice = map.merge("Shoes", 50, function);
        System.out.println("New price: " + newPrice);
        System.out.println("Price of shirt: " + map.merge("Shirt", 100, function));

        map.forEach((key, value) -> System.out.println("Key: " + key + ", value: " + value));
    }*/


    public static void main(String[] args) {
        System.out.println(new Date().getDate());

        String input = "slogan of java language: write once, run everywhere".replaceAll(" ", "");
        Map<Character, Integer> map = new HashMap<>();
        for (char character : input.toCharArray()) {
            Integer temp = map.get(character);
            if (temp != null) {
                map.put(character, temp + 1);
            } else {
                map.put(character, 1);
            }
        }
        int max = 0;
        char result = 0;
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            if (max < entry.getValue()) {
                max = entry.getValue();
                result = entry.getKey();
            }
        }
        System.out.println("Most used symbol: " + result);
    }
}
