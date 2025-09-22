package cycle;

public class Factorial {

    public static int calculate(int number) {
        int result = 1;
        if (number > 0 ) {

            for ( int i = 1 ; i <= number ; i++ ) {

                result =  result * i;

            }
        } else if (number == 0 ) {
            result = 1;
        }
        return result;
    }


}
