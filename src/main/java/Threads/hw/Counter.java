package Threads.hw;

public class Counter implements Runnable {

     private int count;

    public Counter(int count) {
        this.count = count;
    }

    public int get() {
        return count;
    }

    public synchronized int increment() {
        return count++;
    }


    @Override
    public void run() {
        for (int i = 0; i <= 10000; i++) {
           this.increment();
        }
    }




    public static void main(String[] args) throws InterruptedException {

        Counter counter = new Counter(1);

        Thread t1 = new Thread(counter);
        Thread t2 = new Thread(counter);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(counter.get());



    }
}





