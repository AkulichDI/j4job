package io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ByteArrayStream {


    public static void main(String[] args) {

        System.out.println();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] bytes2 = "Message".getBytes();
        outStream.writeBytes(bytes2);
        System.out.println(outStream);

/*
        byte[] bytes = new byte[]{'J', 'a', 'v', 'a'};
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        int data;

        while ( ( data = stream.read()) != -1  ){
            System.out.println((char) data);
        }


 */
    }
}

