package streamApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LS1 {

    public static void main(String[] args) {

        List<String> names = List.of("Dima", "Oleg", "TEst", "Boris", "Alex", "Dima");
        List<String> namesOfDiiimon = names.stream()
                .filter(name -> name.equals("Dima"))
                .toList();

        int countDimon = (int) names.stream()
                        .filter(name -> name.equals("  Dima  "))
                        .filter(name -> name.split(" ").length>0)
                        .count();


        List<String> namesD = names.stream()
                        .filter(name -> name.contains("Di"))
                        .peek(System.out::println)
                        .toList();


        List<String> namesDimons = names.stream()
                        .filter(name -> name.contains("im"))
                                .distinct()
                                        .toList();
        System.out.println(namesDimons);


        List<Integer> id = Arrays.asList(1, 2,3,5,6,7,8,9,0,12,43,65,765,6575,234,2345234,345);
        int idSum = id.stream()
                .mapToInt(Integer::intValue)
                .sum();

        System.out.println(idSum);


        System.out.println(namesOfDiiimon + "\nКоличество Димонов: "  +  countDimon);
    }


}
