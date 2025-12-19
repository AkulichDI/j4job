package streamApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PeekExample {

    public static void main(String[] args) {
        List<StringBuilder> names = Arrays.asList(
                new StringBuilder("Mihail"),
                new StringBuilder("Ivan"),
                new StringBuilder("Елена")
        );
        List<StringBuilder> editedNames = names
                .stream()
                .peek((element -> element.append("(Ученик j4j)")))
                .sorted()
                .toList();

        System.out.println(editedNames);
    }

}
