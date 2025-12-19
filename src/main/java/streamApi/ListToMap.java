package streamApi;

import java.util.*;
import java.util.stream.Collectors;

public class ListToMap {

    public static Map<String, Student> convert(List<Student> list){
        LinkedHashMap<String , Student> students = new LinkedHashMap<>();
        list.stream().collect(
                Collectors.toMap(
                        element -> element.getSurname(),
                        element -> element,
                        (existing, replasment) -> existing));
        return new LinkedHashMap<>();
    }

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<String>();
        Collections.addAll(list, "a=2", "b=3", "c=4", "d==3");

        Map<String, String> result = list.stream()
                .map( e -> e.split("=") )
                .filter( e -> e.length == 2 )
                .collect( Collectors.toMap(e -> e[0], e -> e[1]) );
        System.out.println(result);


        ArrayList<String> list1 = new ArrayList<String>();
        Collections.addAll(list, "a=2", "b=3", "c=4", "d==3");
        String result1 = list.stream().collect( Collectors.joining(", ") );
        System.out.println(result1);

    }



}
