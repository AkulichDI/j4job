package PriorityQueueHMLS;

import java.util.Comparator;
import java.util.PriorityQueue;

public class ScheduleTask {
    private PriorityQueue<Task> queue;

    public ScheduleTask (Comparator<Task> comporator){
        this.queue = new PriorityQueue<>(comporator);
    }

    public void addTask(Task task){
        queue.offer(task);
    }
    public Task readTask(){
        return queue.peek();
    }
    public Task getTask(){
        return queue.poll();
    }



}
