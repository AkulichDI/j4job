package io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReadFile {

    public static void init( ){
        try(FileOutputStream out = new FileOutputStream("data/input.txt")){
            out.write("Login=Login".getBytes());
            out.write( System.lineSeparator().getBytes());
            out.write("Password=Password".getBytes());
            out.write( System.lineSeparator().getBytes());
            out.write("URL=URL".getBytes());
            out.write( System.lineSeparator().getBytes());


        }catch (IOException e ){
            e.printStackTrace();
        }

    }



    public static void main(String[] args) {

        init();

        try ( FileInputStream input = new FileInputStream("data/input.txt") ){

            StringBuilder text = new StringBuilder();
            int read;

            while (( read = input.read()) != -1 ){
                text.append((char) read);
            }
            System.out.println(text.toString());

        }catch (IOException e ){
            e.printStackTrace();
        }



    }
}
