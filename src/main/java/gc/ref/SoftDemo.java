package gc.ref;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class SoftDemo {

    public static void main(String[] args) throws InterruptedException {
        //example1();
       var ls =  new Thread() {
           @Override
           public void run() {
               example2();
           }
       };

       ls.sleep(50000);

       ls.start();

    }


    private static void example1() {

        Object strong = new Object();
        SoftReference<Object> soft = new SoftReference<>(strong);
        strong = null;
        System.out.println(soft.get());

    }

    private static void example2() {

        List<SoftReference<Object>> objects = new ArrayList<>();

        for (int i = 0; i < 100_000_000; i++) {

            objects.add(new SoftReference<Object>(new Object() {

                String value = String.valueOf(System.currentTimeMillis());

                @Override
                protected void finalize() throws Throwable {

                    System.out.println("Object removed!");

                }
            }));

        }

        System.gc();

        int liveObject = 0;

        for (SoftReference<Object> ref : objects) {

            Object object = ref.get();

            if (object != null) {

                liveObject++;

            }

        }

        System.out.println(liveObject);

    }



}
