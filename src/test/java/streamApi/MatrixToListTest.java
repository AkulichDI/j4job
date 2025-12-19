package streamApi;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MatrixToListTest {

    @Test
    public void whenInputSquareMatrix() {
        Integer[][] matrix = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        List<Integer> result = MatrixToList.convert(matrix);
        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertThat(result).containsAll(expected);
    }
    @Test
    public void whenInputNoSquareMatrix() {
        Integer[][] matrix = {
                {1},
                {2, 3},
                {4, 5, 6}
        };
        List<Integer> result = MatrixToList.convert(matrix);
        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);
        assertThat(result).containsAll(expected);
    }





}