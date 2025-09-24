package array;
//     6.6.1
public class LengthArrayArrays {

    public static void main(String[] args) {
        int[][] numbers = {{1}, {4, 2}, {7, 8, 9} ,{4, 5, 6, 5} };
        for (int i = 0; i < numbers.length; i++) {
            System.out.println(
                    "Размер вложенного массива равен: " + numbers[i].length
            );
        }
    }

}
