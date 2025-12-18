package lambda;

import javax.xml.transform.Source;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class CollectionLambdaUsage {

    public static void main(String[] args) {
        Collection<String> collections = new ArrayList<>();

        collections.add("name");
        collections.add("top");
        collections.add("user");
        collections.add("precision");
        collections.add("post");

        Predicate<String> predicate = s -> s.length() == 4;
        collections.removeIf(predicate);
        collections.forEach(System.out::println);
    }
}
class ListLambdaUsage{
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(2,4,5,6,7,8,12);
        UnaryOperator<Integer> operator = i -> i * i;
        list.replaceAll(operator);
        list.forEach(s -> System.out.println("Current number: " + s));
    }
}

class MapLambdaUsage {
    public static void main(String[] args) {
       /* Map<Integer, String> map = new HashMap<>();
        map.put(1,"name");
        map.put(2,"top");
        map.put(3,"user");
        map.put(4,"precision");
        map.put(5,"post");

        BiFunction<Integer, String,String> function = (key, value) ->value + "_" + key;
        map.replaceAll(function);
        map.forEach((key,value) -> System.out.println("Key: " + key + ", value: " + value ));
        */

        /*Map<Integer, String> map = new HashMap<>();
        map.put(1, "name");

        BiFunction<Integer,String,String> function = (key, value) -> value + "_" + key;
        String result = map.computeIfPresent(1,function);
        System.out.println("Current value: " + result);
        map.forEach((key,value)-> System.out.println("Key: " + key + ", value: " + value));
         */


       /* Map<String,Integer> map = new HashMap<>();

        Function<String, Integer> function = String::length;
        map.computeIfAbsent("Petr", function);
        System.out.println("Result: " + map.computeIfAbsent("Petr", function));
        System.out.println("Result: " + map.computeIfAbsent("Petr", key -> key.length() + 10));

        map.forEach((key, value)->System.out.println("Key: " + key + ", value: " + value));
        */

      /*  Map<String, Integer> map = new HashMap<>();
        map.put("Shoes", 200);

        BiFunction<Integer, Integer, Integer> function = (oldValue, newValue ) -> oldValue - newValue;
        int newPrice = map.merge("Shoes", 50, function);
        System.out.println("New price: " + newPrice);
        map.forEach((key,value)->System.out.println("Key: " + key + ", Value: " + value));
        */

        Map<String , Integer> map = new HashMap<>();
        map.put("Shoes", 200);

        BiFunction<Integer,Integer,Integer> function = (oldValue, newValue) -> oldValue - newValue;
        int newPrice = map.merge("Shoes", 50, function);
        System.out.println("New price: "+ newPrice);
        System.out.println("Price of shirt: " + map.merge("Shirt", 100, function));
        map.forEach((key,value)->System.out.println("Key: " + key + ", Value: " + value));
    }
}



class MostUsedSymbol {
    public static void main(String[] args) {
        String input = "slogan of java language: write once, run everywhere".replaceAll(" ", "");
        Map<Character, Integer> map = new HashMap<>();

        for (char character : input.toCharArray()){
           map.merge(character,1,(oldValue,newValue)->oldValue + 1);
        }
        System.out.println(map);

        int max = 0;
        char result = 0;
        for(Map.Entry<Character, Integer> entry : map.entrySet()  ){
            if(max < entry.getValue()){
                max= entry.getValue();
                result = entry.getKey();
            }
        }
        System.out.println("Most used symbol: " + result);
    }





}