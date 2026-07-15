package Threads.hw;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicExample {

    public static void main(String[] args) {
        AtomicInteger counter = new AtomicInteger();

        System.out.println(counter.get());

        int first = counter.incrementAndGet();
        int second = counter.getAndIncrement();

        System.out.println(first);
        System.out.println(second);
        System.out.println(counter.get());
    }

}


class MultiThreadCounter {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            for (int index = 0; index < 100_000; index++) {
                COUNTER.incrementAndGet();
            }
        };

        Thread first = new Thread(task);
        Thread second = new Thread(task);

        first.start();
        second.start();

        first.join();
        second.join();

        System.out.println(COUNTER.get());
    }
}




class ls1 {
    public static void main(String[] args) {

        AtomicInteger counter = new AtomicInteger();
        // Будет 0
        System.out.println(counter.getAndIncrement());
        // Будет 2
        System.out.println(counter.incrementAndGet());
        // Будет 12
        System.out.println(counter.addAndGet(10));

    }
}
class ls2 {

    public static void main(String[] args) throws InterruptedException {

        AtomicInteger counter = new AtomicInteger();

        Runnable task = () -> {
            for (int index = 0; index < 10_000; index++) {
                counter.incrementAndGet();
            }
        };

        Thread f = new Thread(task);
        Thread s = new Thread(task);
        Thread t = new Thread(task);

        f.start();
        s.start();
        t.start();

        f.join();
        s.join();
        t.join();

        System.out.println(counter.get());
    }

}

class ls3 {
    private final AtomicInteger users = new AtomicInteger();

        public boolean addUser() {
            while (true) {
                int start = users.get();
                int end = start + 1;
                if (users.compareAndSet(start, end)) {
                    return true;
                }
            }

        }

}