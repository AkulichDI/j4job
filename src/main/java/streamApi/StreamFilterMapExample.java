package streamApi;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamFilterMapExample {
    public static void main(String[] args) {
        List<String> words = Arrays.asList("кот", "кошка", "тигр", "кит", "компьютер");

        List<String> result = words.stream()
                .filter(w -> w.startsWith("к"))
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        System.out.println(result);
    }
}
