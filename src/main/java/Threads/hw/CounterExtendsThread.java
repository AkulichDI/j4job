package Threads.hw;

public class CounterExtendsThread implements Runnable {

    @Override
    public void run() {
        int i = 0;

        while (!Thread.currentThread().isInterrupted()) {

            try {
                System.out.println(Thread.currentThread().getName() + ": " + i++);
                Thread.sleep(500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }


        }


    }


    public static void main(String[] args) throws InterruptedException {
        CounterExtendsThread counterExtendsThread = new CounterExtendsThread();

       Thread t1 = new Thread(counterExtendsThread);

       t1 .start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t1.interrupt();
        t1.join();

    }
}
