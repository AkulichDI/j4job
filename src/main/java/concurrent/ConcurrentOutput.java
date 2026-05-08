package concurrent;

public class ConcurrentOutput {


    public static void main(String[] args) {
        Thread another = new Thread(
                () ->
                {
                    for (int i = 0; i < 100; i++) {
                        System.out.println(Thread.currentThread().getName());
                    }
                }

        );
        Thread another1 = new Thread(
                () ->
                {
                    for (int i = 0; i < 100; i++) {
                        System.out.println(Thread.currentThread().getName());
                    }
                }

        );
        another.start();
        another1.start();
        System.out.println(Thread.currentThread().getName());
    }



}
