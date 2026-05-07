package algo;

import java.util.Arrays;

public class Main {
    public static int[] squareSortedArray(int[] arr) {
        int n = arr.length;
        int[] result = new int[n];
        int left = 0, right = n - 1;
        int resultIndex = n - 1;

        while (left <= right) {
            if (Math.abs(arr[left]) > Math.abs(arr[right])) {
                result[resultIndex] = arr[left] * arr[left];
                left++;
            } else {
                result[resultIndex] = arr[right] * arr[right];
                right--;
            }
            resultIndex--;
        }

        return result;
    }







    public static void main(String[] args) {
        int[] sortedArray = {-4, -2, 0, 2, 3, 5};
        int[] resultArray = squareSortedArray(sortedArray);
        System.out.println(Arrays.toString(resultArray));
    }
}