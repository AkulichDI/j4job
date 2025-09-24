package array;
//6.8.0
public class TwoNumberSum {

    public static int[] getIndexes(int[] array, int target) {
        int i = 0;
        int j = 1 ;
        while (i < array.length){
            if (array[i] + array[j] == target){
                return new int[] {i, j};

            }if (array[i] + array[j] != target) {
                j++;

            }if (j == array.length){
                j = i +1;
                i++;
            }
        }
        return new int[0];
    }

}
