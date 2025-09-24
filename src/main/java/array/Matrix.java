package array;
//6.6.4
import java.lang.reflect.Array;

public class Matrix {

    public static int[][] multiple(int size) {
        int[][] table = new int[size][size];

        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {

                table[i][j] = (i + 1) * (j + 1);


            }

        }



        return table;
    }



}
