package array;

public class SortSelected {

    // Тема 6.5.3


    public static int[] sort(int[] data) {
        for (int i = 0; i<data.length; i++) {
            int min = MinDiapason.findMin(data,i, data.length - 1);
            int index = FindLoop.indexInRange(data, min,i, data.length - 1);
            /* swap(...) */
            int tmp = data[i];
            data[i] = min;
            data[index] = tmp;



        }
        return data;
    }



}
