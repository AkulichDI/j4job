package collections;

import java.util.HashSet;

public class UniqueText {

    public boolean isEquals(String originText, String duplicateText) {

        boolean result = true;
        String[] origin = originText.split(" ");
        String[] text = duplicateText.split(" ");
        HashSet<String> check = new HashSet<>();

        for (String x : origin){
            check.add(x);
        }

        for (String y : text){
            boolean t =  check.contains(y);
            if (!t){

                return false;
            }
        }
        /* for-each origin -> new HashSet. */
        /* for-each text -> hashSet.contains */
        return result;
    }
}
