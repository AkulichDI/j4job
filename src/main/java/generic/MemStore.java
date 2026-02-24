package generic;

import java.util.HashMap;
import java.util.Map;

public final class MemStore<T extends Base> implements Store<T> {
    private final Map<String, T> storage = new HashMap<>();

    @Override
    public void add(T model) {
        if ( !storage.containsKey(model.getId())) {
            storage.put(model.getId(), model);
        }

    }
    @Override
    public boolean replace(String id, T model) {
        if (storage.containsKey(id)) {
            storage.replace(id , model);
            return true;
        }
        return false;
    }
    @Override
    public void delete(String id) {
        storage.remove(id);
    }

    @Override
    public T findById(String id) {
        if (storage.containsKey(id)) {
            return storage.get(id);
        }
        return null;

    }
}