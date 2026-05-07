package algo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SmallestRangeFinder {


    /** Добавьте поля класса здесь, если это необходимо */

    public static int[] findSmallestRange(int[] nums, int k) {
        /** Добавьте реализацию метода здесь
         Метод должен принимать массив nums и целочисленное значение k,
         и возвращать массив из двух целых чисел, представляющих наименьший диапазон
         с k различными элементами в массиве nums
         Если такой диапазон найти невозможно, вернуть null
                                                    **/
        if (nums == null || k <= 0) {
            return null;
        }

        int bestLeft = -1;
        int bestRight = -1;
        int bestLength = Integer.MAX_VALUE;

        for (int left = 0; left < nums.length; left++) {
            Set<Integer> set = new HashSet<>();

            for (int right = left; right < nums.length; right++) {
                set.add(nums[right]);

                if (set.size() == k) {
                    int currentLength = right - left + 1;

                    if (currentLength < bestLength) {
                        bestLength = currentLength;
                        bestLeft = left;
                        bestRight = right;
                    }

                    break;
                }
            }
        }

        if (bestLeft == -1) {
            return null;
        }

        return new int[]{bestLeft, bestRight};


        /** замените это на вашу реализацию */
    }

    public static void main(String[] args) {
        int[] nums = {1, 3, 5, 7, 9};
        int k = 3;
        int[] result = findSmallestRange(nums, k);
        if (result != null) {
            System.out.println("Наименьший диапазон с " + k + " различными элементами: " + Arrays.toString(result));
        } else {
            System.out.println("Такой диапазон не существует.");
        }
    }



}
