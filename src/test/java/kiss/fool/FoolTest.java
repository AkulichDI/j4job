package kiss.fool;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FoolTest {


    @Test
    public void whenNumberIs1Then1() {
        assertEquals("1", Fool.answer(1));
    }

    @Test
    public void whenNumberIs3ThenFizz() {
        assertEquals("Fizz", Fool.answer(3));
    }

    @Test
    public void whenNumberIs5ThenBuzz() {
        assertEquals("Buzz", Fool.answer(5));
    }

    @Test
    public void whenNumberIs15ThenFizzBuzz() {
        assertEquals("FizzBuzz", Fool.answer(15));
    }

    @Test
    public void whenNumberIs30ThenFizzBuzz() {
        assertEquals("FizzBuzz", Fool.answer(30));
    }


}