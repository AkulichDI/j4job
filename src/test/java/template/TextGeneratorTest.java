package template;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextGeneratorTest {

    @Test
    void whenTemplateHasKeysThenReplaceThem() {
        Generator generator = new TextGenerator();

        String template = "I am a ${name}, Who are ${subject}? ";
        Map<String, String> args = Map.of(
                "name", "Petr Arsentev",
                "subject", "you"
        );

        String result = generator.produce(template, args);

        assertEquals("I am a Petr Arsentev, Who are you? ", result);
    }

    @Test
    void whenTemplateHasKeysButMapIsEmptyThenThrowException() {
        Generator generator = new TextGenerator();

        String template = "I am a ${name}, Who are ${subject}? ";
        Map<String, String> args = new HashMap<>();

        assertThrows(
                IllegalArgumentException.class,
                () -> generator.produce(template, args)
        );
    }

    @Test
    void whenMapHasExtraKeyThenThrowException() {
        Generator generator = new TextGenerator();

        String template = "I am a ${name}";
        Map<String, String> args = Map.of(
                "name", "Petr Arsentev",
                "subject", "you"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> generator.produce(template, args)
        );
    }
}