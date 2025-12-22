package search;

import java.util.ArrayList;
import java.util.function.Predicate;

public class PhoneDictionary {

    private ArrayList<Person> persons = new ArrayList<Person>();

    public void add (Person person){
        this.persons.add(person);
    }
    public ArrayList<Person> find (String key){
        Predicate<Person> name = person -> person.getName().contains(key);
        Predicate<Person> surname = person -> person.getSurname().contains(key);
        Predicate<Person> phone = person -> person.getPhone().contains(key);
        Predicate<Person> address = person -> person.getAddress().contains(key);

        Predicate<Person> combine = name.or(surname.or(phone.or(address)));
        ArrayList<Person> result = new ArrayList<>();
        for (var person : persons) {
            if (combine.test(person)) {
                result.add(person);
            }
        }

        return result;
    }


}
