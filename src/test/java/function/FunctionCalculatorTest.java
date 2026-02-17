package function;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class FunctionCalculatorTest {
    @Test
    void whenLinearFunctionThenLinearResults() {
        FunctionCalculator calc = new FunctionCalculator();
        List<Double> result = calc.diapason(5, 8, x -> 2 * x + 1);
        List<Double> expected = Arrays.asList(11D, 13D, 15D);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenQuadraticFunctionThenQuadraticResults() {
        FunctionCalculator calc = new FunctionCalculator();
        List<Double> result = calc.diapason(1, 4, x -> x * x + 2 * x + 1);
        List<Double> expected = Arrays.asList(4D, 9D, 16D);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenExponentFunctionThenExponentResults() {
        FunctionCalculator calc = new FunctionCalculator();
        List<Double> result = calc.diapason(0, 3, x -> Math.pow(2, x));
        List<Double> expected = Arrays.asList(1D, 2D, 4D);
        assertThat(result).isEqualTo(expected);
    }
}