package collections;

import java.util.Objects;

public class User implements Comparable<User> {
    private String name;
    private int age;


    public User (String name, int age){
        this.name = name;
        this.age = age;
    }
    @Override
    public int compareTo(User o) {

        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        User user = (User) object;
        return age == user.age && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
