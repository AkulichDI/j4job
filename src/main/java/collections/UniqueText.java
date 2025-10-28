package collections;

import java.util.HashSet;

public class UniqueText {

    public boolean isEquals(String originText, String duplicateText) {

        boolean result = true;
        String[] origin = originText.split(" ");
        String[] text = duplicateText.split(" ");
        HashSet<String> check = new HashSet<>();

        /* for-each origin -> new HashSet. */
        /* for-each text -> hashSet.contains */
        return result;
    }
}
