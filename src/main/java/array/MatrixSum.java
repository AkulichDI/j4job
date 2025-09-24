package array;
//6.6.2
public class MatrixSum {

        public static int sum(int[][] array) {
            int result = 0;

            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    result += array[i][j];
                }
            }


            return result;
        }

}
