package array;

public class EndsWith {

    public static boolean endsWith(char[] word, char[] postfix) {
        boolean result = true;

        int counter = 0;
        for (int i = 0; i < postfix.length; i++) {
            if (postfix[postfix.length - (1 + i)] == word[word.length - (1 + i)]){
                counter++;
            }else {
                result = false;
                break;
            }
        }

        /* проверить. что массив word имеет последние элементы одинаковые с postfix */
        return result;
    }


}
