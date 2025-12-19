package streamApi;

import java.util.*;
import java.util.stream.IntStream;

public class MinExample {

    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(4,5,1,3,2);
        Optional<Integer> minEl = list.stream().min(Comparator.naturalOrder());
        System.out.println(minEl.get());

        OptionalInt min = IntStream
                .rangeClosed(1,5)
                .min();
        System.out.println(min.getAsInt());


        List<Person> people = Arrays.asList(
                new Person("Михаил", 35),
                new Person("Ольга", 26),
                new Person("Антон", 20),
                new Person("Виктор", 16),
                new Person("Анна", 29)
        );
        Optional<Person> youngPerson = people.stream()
                .min(Comparator.comparing(Person::getAge));
        int minAge = youngPerson.get().getAge();


        Optional<Person> oldPerson = people.stream()
                .max(Comparator.comparing(Person::getAge));
        int oldAge = oldPerson.get().getAge();

        System.out.println(oldAge);



    }

}
