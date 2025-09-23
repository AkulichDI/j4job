package array;



public class EqualLast {

    public static boolean check(int[] left, int[] right) {
        boolean result = true;

     /*   int counter = 0;
        for (int i = left.length - 1; i > left.length-2; i--) {
            if (left[i] == right[i]){
                break;
            }else {
                result = false;
                break;
            }
        }*/
        /*                Посмотрел коммент                      */
        return left[left.length - 1] == right[right.length - 1];



    }

}
