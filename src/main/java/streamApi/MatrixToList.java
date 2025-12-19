package streamApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MatrixToList {


    public static List<Integer> convert(Integer[][] matrix){
        return Arrays.stream(matrix)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }
}
