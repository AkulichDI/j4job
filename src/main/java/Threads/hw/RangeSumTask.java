package Threads.hw;

public class RangeSumTask implements  Runnable {
    private final String taskName;
    private final int start;
    private final int finish;

    public RangeSumTask(String taskName, int start, int finish) {
        this.taskName = taskName;
        this.start = start;
        this.finish = finish;
    }


    @Override
    public void run() {

        int sum = 0;
        for (int i = start; i <= finish; i++) {
            sum += i;
        }
        System.out.println(taskName + ": " + sum);

    }


    public static void main(String[] args) {

        Thread task1 = new  Thread(new RangeSumTask("task1", 10, 12));
        Thread task2 = new  Thread(new RangeSumTask("task2", 10, 10));

        task1.start();
        task2.start();



    }

}
