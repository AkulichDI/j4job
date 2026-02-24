package generic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RoleStoreTest {

    @Test
    void whenAddAndFindThenRoleNameIsAdmin() {
        RoleStore store = new RoleStore();
        store.add(new Role("1", "Admin"));
        Role result = store.findById("1");
        assertThat(result.getRoleName()).isEqualTo("Admin");
    }
    @Test
    void whenAddAndFindThenRoleIsNull() {
        RoleStore store = new RoleStore();
        store.add(new Role("1", "Admin"));
        Role result = store.findById("10");
        assertThat(result).isNull();
    }
    @Test
    void whenAddDuplicateAndFindThenRoleNameIsSuperUser() {
        RoleStore store = new RoleStore();
        store.add(new Role("1", "Admin"));
        store.add(new Role("1", "SuperUser"));
        Role result = store.findById("1");
        assertThat(result.getRoleName()).isEqualTo("Admin");
    }
    @Test
    void whenReplaceThenRoleNameIsGen() {
        RoleStore store = new RoleStore();
        store.add(new Role("1", "Admin"));
        store.replace("1", new Role("1", "Gen"));
        Role result = store.findById("1");
        assertThat(result.getRoleName()).isEqualTo("Gen");
    }
    @Test
    void whenNoReplaceUserThenNoChangeUsername() {
        RoleStore store = new RoleStore();
        store.add(new Role("1", "Admin"));
        store.replace("10", new Role("10", "reader"));
        Role result = store.findById("1");
        assertThat(result.getRoleName()).isEqualTo("Admin");
    }
}