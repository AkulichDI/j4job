package Threads;

public class JoinExample {

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            System.out.println("Thread started");
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
            System.out.println("Thread ending");
        });
        t.start();
        System.out.println("Waiting for thread");
       //
            t.join(); // ждем окончания t
        System.out.println("Main ends");
    }

}
