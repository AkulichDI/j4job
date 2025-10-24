package collections;

import java.sql.Array;
import java.util.ArrayList;

public class PhoneDictionary {

    private ArrayList<Person> persons = new ArrayList<>();

    public void add(Person person){
        this.persons.add(person);
    }


    /**
     * Вернуть список всех пользователей, который содержат key в любых полях.
     * @param key Ключ поиска.
     * @return Список пользователей, список пользователей, которые прошли проверку.
     */



    public  ArrayList<Person> find (String key) {
        ArrayList<Person> result = new ArrayList<>();
        for ( Person person : persons ){
            if (person.toString().contains(key)){
                    result.add(person);
            }
        }
        return result;
    }

}
