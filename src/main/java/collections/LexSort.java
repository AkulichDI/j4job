package collections;

import java.util.Comparator;

public class LexSort implements Comparator<String> {
    @Override
    public int compare(String left, String right) {
        int leftNum = Integer.parseInt(left.split("\\.")[0]);
        int rightNum = Integer.parseInt(right.split("\\.")[0]);
        return Integer.compare(leftNum, rightNum);
    }


}
