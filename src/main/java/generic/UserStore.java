package generic;

public class UserStore implements Store<User> {

    private final Store<User> store = new MemStore<>();

    @Override
    public void add(User model) {

    }
    @Override
    public boolean replace(String id, User model) {

        return false;

    }
    @Override
    public void delete(String id) {


    }
    @Override
    public User findById(String id) {
        return null;
    }
}