package Threads;

import java.util.ArrayList;

public class MyThread extends Thread {

    public static ArrayList<String> names = new ArrayList<>();
    int counter = 0;
    @Override
    public void run() {

        for (String name : names){
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
