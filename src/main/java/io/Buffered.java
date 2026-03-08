package io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Buffered {


    public static void main(String[] args) {

        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream("data/newData.txt"));
             BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream("data/output.txt", true))){

            output.write(input.readAllBytes());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}
