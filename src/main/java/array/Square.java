package array;

public class Square {

    public static int[] calculate(int bound) {
        int[] result = new int[bound];
        for (int i = 0; i < result.length; i++) {

            result[i] = pow(i);

        }
        return result;
    }

    public static int pow(int number){
        return number * number;
    }

    public static void main(String[] args) {
        int[] array = calculate(5);
        for (int i : array) {
            System.out.println(i);
        }
    }


}
