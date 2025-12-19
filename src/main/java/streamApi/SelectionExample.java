package streamApi;

import java.util.Arrays;
import java.util.List;

public class SelectionExample {
    public static void main(String[] args) {
        List<String> strings = Arrays.asList("Один", "Два", "Три", "Четыре", "Пять");
        List<String> result = strings
                .stream()
                .skip(2)
                .limit(2)
                .toList();
        System.out.println(result);


        String result1 = strings
                .stream()
                .skip(7)
                .limit(1)
                .findFirst()
                .orElse("Ноу дата");
        System.out.println(result1);


        String result2 = strings
                .stream()
                .skip(strings.size()- 1 )
                .limit(1)
                .findFirst()
                .orElse("Ноу дата");
        System.out.println(result2);
    }
}
