package streamApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LS1 {

    public static void main(String[] args) {

        List<String> names = List.of("Dima", "Oleg", "TEst", "Boris", "Alex", "Dima");
        List<String> namesOfDiiimon = names.stream()
                .filter(name -> name.equals("Dima"))
                .toList();


        System.out.println(namesOfDiiimon);
    }


}
