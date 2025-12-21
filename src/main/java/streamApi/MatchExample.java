package streamApi;

import java.util.Arrays;
import java.util.List;

public class MatchExample {

    public static void main(String[] args) {
        List<String> strings = Arrays.asList("Один", "Два", "Три", "Четыре", "Пять");
        boolean result = strings.stream()
                .noneMatch("Шесть"::contains);
        System.out.println(result);

        boolean result1 = strings.stream()
                .anyMatch(element -> element.endsWith("ь"));
        // .anyMatch("Один"::contains);
        System.out.println(result1);

        List<String> strings1 = Arrays.asList("Трижды", "Три", "Триста", "Три тысячи");

        boolean result2 = strings1.stream()
                .allMatch(element -> element.startsWith("Три"));
        System.out.println(result2);

    }
}
