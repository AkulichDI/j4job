package io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class PrintUsage {


    public static void main(String[] args) {

        try (PrintStream stream = new PrintStream(new FileOutputStream("data/print.txt"));
             PrintWriter writer = new PrintWriter("data/write.txt")) {
            stream.println("Из PrintStream в FileOutputStream");

            stream.write("Новая строка".getBytes());

            writer.println("Новое сообщение");

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}
