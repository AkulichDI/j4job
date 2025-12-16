package collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class StringCompareTest {
    @Test
    public void whenStringsAreEqualThenZero() {
        StringCompare compare = new StringCompare();
        int result = compare.compare(
                "Ivanov",
                "Ivanov"
        );
        assertThat(result).isEqualTo(0);
    }
    @Test
    public void whenLeftLessThanRightResultShouldBeNegative() {
        StringCompare compare = new StringCompare();
        int result = compare.compare(
                "Ivanov",
                "Ivanova"
        );
        assertThat(result).isLessThan(0);
    }
    @Test
    public void whenLeftGreaterThanRightResultShouldBePositive() {
        StringCompare compare = new StringCompare();
        int result = compare.compare(
                "Petrov",
                "Ivanova"
        );
        assertThat(result).isGreaterThan(0);
    }
    @Test
    public void secondCharOfLeftGreaterThanRightShouldBePositive() {
        StringCompare compare = new StringCompare();
        int result = compare.compare(
                "Petrov",
                "Patrov"
        );
        assertThat(result).isGreaterThan(0);
    }
    @Test
    public void secondCharOfLeftLessThanRightShouldBeNegative() {
        StringCompare compare = new StringCompare();
        int result = compare.compare(
                "Patrova",
                "Petrov"
        );
        assertThat(result).isLessThan(0);

    }

    @Test
    public void whenEmptyAndNonEmptyThenNegative() {
        StringCompare compare = new StringCompare();
        int result = compare.compare(
                "",
                "Ivanov"
        );
        assertThat(result).isLessThan(0);
    }

    @Test
    public void whenNonEmptyAndEmptyThenPositive() {
        StringCompare compare = new StringCompare();
        int result = compare.compare(
                "Ivanov",
                ""
        );
        assertThat(result).isGreaterThan(0);
    }

    @Test
    public void whenFirstCharDiffThenNegative() {
        StringCompare compare = new StringCompare();
        int result = compare.compare(
                "ABC",
                "B"
        );
        assertThat(result).isLessThan(0);
    }



}