package io;

import java.io.ByteArrayInputStream;

public class ByteArrayStream {


    public static void main(String[] args) {




        byte[] bytes = new byte[]{'J', 'a', 'v', 'a'};
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        int data;

        while ( ( data = stream.read()) != -1  ){
            System.out.println((char) data);
        }

    }
}
