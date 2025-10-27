package collections;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConvertListTest {
    @Test
    public void whenTwoList() {
        List<int[]> list = new ArrayList<>();
        list.add(new int[] {1});
        list.add(new int[] {2, 3});
        List<Integer> result = ConvertList.convert(list);
        List<Integer> expected = Arrays.asList(1, 2, 3);
        assertThat(result).containsAll(expected);
    }
}