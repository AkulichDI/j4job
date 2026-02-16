package streamApi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class SortWithLambda {

    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("Ирина", "Аня", "Максим", "Борис"));

        // Сортировка по длине строки (короче → раньше)
        names.sort((a, b) -> Integer.compare(a.length(), b.length()));

        System.out.println(names);
    }
}
