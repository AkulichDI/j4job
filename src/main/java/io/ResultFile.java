package io;

import java.io.FileOutputStream;
import java.io.IOException;

public class ResultFile {

    public static void main(String[] args) {

        try (FileOutputStream output = new FileOutputStream("data/dataResult.txt")) {
            output.write("Hello world".getBytes());
            output.write(System.lineSeparator().getBytes());
            System.out.println(output.toString());

        }catch (IOException e ){
            e.printStackTrace();
        }

    }

}
