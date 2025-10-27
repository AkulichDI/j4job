package collections;

import java.util.List;


public class ConvertList2Array {

    public static int[][] toArray(List<Integer> list, int cells) {
        int groups = (int) Math.ceil((double) list.size() / cells);
        int[][] array = new int[groups][cells];
        int index = 0;

            for (int i = 0; i < groups; i++) {
                for (int j = 0; j < cells; j++) {
                    if (list.size() > index) {
                        array[i][j] = list.get(index++);
                    }else {
                        array[i][j] = 0;
                    }

                }
            }


        return array;
    }


    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7);
        int[][] result = toArray(list, 3);
        for (int[] row : result) {
            for (int cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }


}
