package cycle;

public class Counter {

    public static long sum(int start, int finish) {
        long sum = 0;
        for ( int i = start; i <= finish; i++ ) {
            sum = sum + i;
        }
        return sum;

    }


    public static int sumByEven(int start, int finish) {
        int sum = 0;
        for ( int i = start; i <= finish; i++  ){
            if ( i % 2 == 0 ){
                sum = sum + i;
            }else{
                continue;
            }
        }
        return sum;
    }


    public static void main(String[] args) {
        System.out.println(sum(-100, 100));
        System.out.println(sum(0, 999999999));
        System.out.println(sum(1, 1));



        System.out.println(sumByEven(1, 10));
        System.out.println(sumByEven(3, 8));
        System.out.println(sumByEven(-100, 1));
    }

}
