package OperatorVetvlenia;
// 6 задание
public class Max {

    public static int max(int left, int right) {
        int result = left > right? left: right;
        return result;
    }
    public static int summation(int first, int second) {
        int result = first + second;
        return result;
    }

    public static void main(String[] args) {

        int left = 4;
        int rright = 3;
        int max = Max.max(left, rright);
        int sum = Max.summation(left, rright);
        System.out.println(max);


    }


}
