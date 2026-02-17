package Threads;

public class Main {
    public static void main(String[] args) {
        MyThread.names.add("Дима");
        MyThread.names.add("Халя");
        MyThread.names.add("Гэга");
        MyThread.names.add("Гога");
        MyThread.names.add("Гиги");



        for (int i = 0; i<5; i++){
          //  var runnable = new MyRunnable(i);
           // var thread   = new Thread(runnable);
           // thread.start();
        }

        /*

        Thread thread2 = new MyThread();

        Thread thread = new MyThread();
        thread.start();
        thread2.start();*/
    }
}
