package array;

public class Check {

//                      Т_Т
    public static boolean mono(boolean[] data) {
        int counterf = 0;
        int countert = 0;
        boolean test = data[0];
        boolean result = true;
        for (int i = 0; i < data.length; i++) {

            if (test ==  data[i]){
                countert++;
            }else{
                counterf++;
            }

        }
        if (counterf == data.length){

             result = false;
        }else if (countert == data.length) {

             result = true;
        }else {
            result = false;
        }


        return result;
    }




}
