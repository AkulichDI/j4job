package collections;

import java.util.*;

public class FullSearch {

    public Set<String> extractNumber (List<Task> tasks){
        HashSet<String> result = new HashSet<>();
        for (Task x : tasks){
            result.add(x.getNumber());
        }
        return result;
    }

    public static void main(String[] args) {

    }

}
