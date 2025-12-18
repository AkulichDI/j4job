package streamApi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class School {

    public List<Student> collect(List<Student> students, Predicate<Student> predicate){
        return  students.stream()
                .filter(student -> predicate.test(student))
                .collect(Collectors.toList());
    }



}
