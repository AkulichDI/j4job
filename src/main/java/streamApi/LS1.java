package streamApi;

import java.util.ArrayList;
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









        System.out.println(namesOfDiiimon + "\nКоличество Димонов: "  +  countDimon);
    }


}
