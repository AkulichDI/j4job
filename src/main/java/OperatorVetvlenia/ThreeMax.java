package OperatorVetvlenia;

public class ThreeMax {
    public static int max(int first, int second, int third) {
        int result = first;
        if (first >= second && first >= third) {
            result = first;
        }
        else if (second >= first && second >= third) {
            result = second;
        }else {
            result = third;
        }
        return result;
    }

    public static void main ( String[] args ) {
        int first = 10;
        int second = 10;
        int thrid =1;
        int max = max ( first,second,thrid );
        System.out.println (max);
    }
}
