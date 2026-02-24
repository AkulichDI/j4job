package generic;

import java.util.HashMap;
import java.util.Map;

public final class MemStore<T extends Base> implements Store<T> {
    private final Map<String, T> storage = new HashMap<>();

    @Override
    public void add(T model) {

    }
    @Override
    public boolean replace(String id, T model) {

        return false;

    }
    @Override
    public void delete(String id) {

    }
    @Override
    public T findById(String id) {

        return null;

    }
}