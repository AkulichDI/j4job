package array;

public class ArrayChar {

    public static boolean startsWith(char[] word, char[] prefix) {
        boolean result = true;
        int counter = 0;
        for (int i = 0; i < prefix.length; i++) {
            if (prefix[i] == word[i]){
                counter++;
            }else {
                break;
            }
        }
        result = counter == prefix.length;

        return result;
    }


}
