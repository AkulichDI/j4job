package Threads.hw;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
class Ls2Test {




        @RepeatedTest(1_000)
        void shouldExecuteActionsInStrictOrder()
                throws InterruptedException {

            Less2.OrderedPipeline pipeline = new Less2.OrderedPipeline();
            StringBuffer result = new StringBuffer();

            List<Thread> threads = new ArrayList<>();

            threads.add(new Thread(
                    () -> pipeline.first(() -> result.append("1"))
            ));

            threads.add(new Thread(
                    () -> pipeline.second(() -> result.append("2"))
            ));

            threads.add(new Thread(
                    () -> pipeline.third(() -> result.append("3"))
            ));

            Collections.shuffle(threads);

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertEquals("123", result.toString());
        }
    }

