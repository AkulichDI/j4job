package Threads;

import static Threads.MyThread.names;
public class MyRunnable implements Runnable{
    private int i;
  MyRunnable(int i ){
      this.i = i;
  }

  int counter = 0;
    @Override
    public void run() {
        for (String name : MyThread.names){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Имя чела из массива:  " + name + " его порядковый номер: " + counter);
            counter++;
        }
        counter = 0;
    }
}
