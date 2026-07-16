package Threads.hw;

public class Less2 {

    class Counter {


        final Counter lock = new Counter();

        private int value;

        public void increment(){
            synchronized (lock) {
                value++;
            }

        }
    }

    /**
     * Задача 1. Ordered Pipeline
     *
     * Три метода вызываются тремя разными потоками в произвольном порядке:
     *
     * first()
     * second()
     * third()
     *
     * Нужно гарантировать порядок выполнения действий:
     *
     * first → second → third
     * Ограничения:
     *
     * нельзя использовать synchronized;
     * нельзя использовать Lock;
     * нельзя использовать AtomicInteger;
     * нельзя использовать wait() / notify();
     * нельзя использовать Semaphore, CountDownLatch;
     * нельзя использовать Thread.sleep();
     * разрешены volatile и Thread.onSpinWait();
     * каждый метод вызывается ровно один раз.
     */
    public static final class OrderedPipeline {

        private volatile int phase;

        public void first(Runnable action) {

            action.run();
            phase = 1;
        }

        public void second(Runnable action) {
            while (phase != 1) {
                Thread.onSpinWait();
            }
            action.run();
            phase = 2;
        }

        public void third(Runnable action) {
            while (phase != 2) {
                Thread.onSpinWait();
            }
            action.run();
            phase = 3;
        }
    }


}
