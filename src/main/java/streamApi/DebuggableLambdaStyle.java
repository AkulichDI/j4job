package streamApi;

import java.util.Arrays;
import java.util.List;

public class DebuggableLambdaStyle {

    public static void main(String[] args) {
        List<String> words = Arrays.asList("кот", "кошка", "тигр");

        // Вместо сложной лямбды — метод reference:
        words.stream()
                .map(DebuggableLambdaStyle::decorate)
                .forEach(System.out::println);
    }

    private static String decorate(String s) {
        // тут легко поставить breakpoint
        return "[-[" + s.toUpperCase() + "]-]";
    }
}
