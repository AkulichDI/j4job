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
            result.add(person);
            /*if (person.toString().contains(key)){
                    result.add(person);
            }*/

            System.out.println(person.toString());
        }
        return result;
    }

    public static void main(String[] args) {
        PhoneDictionary p = new PhoneDictionary();
        System.out.println(p.find(""));
    }

}
