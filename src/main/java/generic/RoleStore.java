package generic;

public class RoleStore implements Store<Role>{

    private final Store<Role> store = new MemStore<>();

    @Override
    public void add(Role model) {
        store.add(model);
    }

    @Override
    public boolean replace(String id, Role model) {
        if (store.replace(id,model))return true;
        return false;
    }
    @Override
    public void delete(String id) {
        store.delete(id);
    }
    @Override
    public Role findById(String id) {
        return store.findById(id);
    }
}
