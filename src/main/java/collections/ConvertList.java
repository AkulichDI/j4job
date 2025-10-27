package collections;

import java.util.ArrayList;
import java.util.List;

public class ConvertList {
    public static List<Integer> convert(List<int[]> list) {

        List<Integer> result = new ArrayList<>();

        /* for-each */
        for (int[] x : list){
            for (int y : x){
                result.add(y);
            }
        }

        return result;

    }
}
