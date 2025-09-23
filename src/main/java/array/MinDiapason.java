package array;

public class MinDiapason {

    public static int findMin(int[] array, int start, int finish) {
        int min = array[start];

        for (int i = start + 1; i <= finish; i++) {
            if (min > array[i]){

                min = array[i];

            }else {
                continue;
            }
        }

        return min;
    }

}
