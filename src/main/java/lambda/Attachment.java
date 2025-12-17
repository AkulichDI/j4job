package lambda;

public class Attachment {

    public Attachment(String name, int size){
        this.name = name;
        this.size = size;
    }


    private String name;
    private int size;

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }
}
