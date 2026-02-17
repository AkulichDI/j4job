package Threads;

class MyRunnable implements Runnable {
    public void run() {
        System.out.println("This is a new thread.");
    }
}

public class RunnableExample {
    public static void main(String[] args) {
        MyRunnable myRunnable = new MyRunnable();
        Thread thread = new Thread(myRunnable); // Создаем новый поток
        thread.start(); // Запускаем новый поток
    }
}