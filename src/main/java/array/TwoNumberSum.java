package array;

public class TwoNumberSum {

    public static int[] getIndexes(int[] array, int target) {
        int i = 0;
        int j = i + 1 ;
        while (i < j){
            if (array[i] + array[j] == target){

                return new int[] {i, j};


            }
            j++;
            i++;
        }



        return new int[0];
    }

}
