package PriorityQueueHMLS;

import java.util.Comparator;

public class TaskByPositionAsc implements Comparator<Task> {

    @Override
    public int compare(Task left, Task right) {
        return left.position().compareTo(right.position());
    }
}
