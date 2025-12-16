package PriorityQueueHMLS;

import java.util.Comparator;

public class TaskByUrgencyDesc implements Comparator<Task> {
    @Override
    public int compare(Task left, Task right) {
        return Integer.compare(right.urgency(), left.urgency());
    }
}
