package gc;

import java.util.Random;

public class GCTypeDemo {

    public static void main(String[] args) {

        Random rand = new Random();
        int length = 100;

        String[] data = new String[1_000_000];

        for (int i = 0; ;i = ( i + 1 ) % data.length ) {

            data[i] = String.valueOf(
                    (char) rand.nextInt(255)
            ).repeat(length);

        }

    }


}
