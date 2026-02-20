package Threads;

public class ThreadRunnableTest {






}
class CounterThread extends Thread {
    private int x = 0;
    public void run() {
        while (x < 3) {
            System.out.println("Thread: " + x);
            x++;
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
    }
}

class CounterRunnable implements Runnable {
    private int x = 0;
    public void run() {
        while (x < 2) {
            System.out.println("Runnable: " + x);
            x++;
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
    }
}

 class ThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new CounterThread();
        Thread t2 = new Thread(new CounterRunnable());
        t1.start();  // запускает CounterThread.run()
        t2.start();  // запускает CounterRunnable.run()
        System.out.println("Возвращаем текущий поток: " + Thread.currentThread());
        System.out.println("количество потоков: " +  Thread.activeCount());
        t1.join();
        t2.join();
        System.out.println("Done");
    }
}
