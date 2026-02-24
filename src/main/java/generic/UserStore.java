package generic;

import java.util.Collections;
import java.util.Iterator;

public class UserStore implements Store<User> {

    private final Store<User> store = new MemStore<>();

    @Override
    public void add(User model) {
        store.add(model);
    }
    @Override
    public boolean replace(String id, User model) {
        if (store.replace(id,model))return true;
        return false;
    }
    @Override
    public void delete(String id) {
            store.delete(id);
    }
    @Override
    public User findById(String id) {
        return store.findById(id);
    }
}