package generic;

public class Role extends Base{


    private final String rolename;


    public Role(String id, String rolename) {
        super(id);
        this.rolename = rolename;
    }

    public String getRoleName() {
        return rolename;
    }
}
