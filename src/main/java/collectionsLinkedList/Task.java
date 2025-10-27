package collectionsLinkedList;

public class Task {
    private String description;
    private int priority;

    public Task (String description, int priority){
        this.description = description;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

}
