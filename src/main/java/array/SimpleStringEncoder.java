package array;

public class SimpleStringEncoder {

    public static String encode(String input) {
        String result = "";
        char symbol = input.charAt(0);
        int counter = 1;
        for (int i = 1; i < input.length(); i++) {

            if (symbol == input.charAt(i)) {
                counter++;
            } else if (symbol != input.charAt(i)) {
                result +=  symbol + "" + counter;
                counter = 0;
                symbol = input.charAt(i);
            }
        }
        return result;
    }
}
