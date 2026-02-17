package Threads;

 class MyThreadT extends Thread {
    public void run() {
        System.out.println("This is a new thread.");
    }
}

public class ThreadExample {
    public static void main(String[] args) {

        for (int i = 0; i < 6; i++) {
            MyThreadT myThread = new MyThreadT();
            myThread.start();
        }
        System.out.println("Complete");
    }
}