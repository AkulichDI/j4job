package streamApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlatMapToIntExample {

    public static void main(String[] args) {
        int[] array1 = {1,2,3};
        int[] array2 = {4,5,6};
        int[] array3 = {7,8,9};

        List<int[]> arrays = new ArrayList<>();
        arrays.add(array1);
        arrays.add(array2);
        arrays.add(array3);

        List<Integer> list = arrays.stream()
                .flatMapToInt(Arrays::stream)
                .boxed()
                .collect(Collectors.toList());
        System.out.println(list);

    }
}
