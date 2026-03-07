package io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class EvenNumberFile {

    public static void init( ){
        try(FileOutputStream out = new FileOutputStream("data/event.txt")){
            out.write("1".getBytes());
            out.write( System.lineSeparator().getBytes());
            out.write("6".getBytes());
            out.write( System.lineSeparator().getBytes());
            out.write("14".getBytes());
            out.write( System.lineSeparator().getBytes());
            out.write("17".getBytes());
            out.write( System.lineSeparator().getBytes());

        }catch (IOException e ){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        init();

        try (FileInputStream input = new FileInputStream("data/event.txt")){
            StringBuilder sb = new StringBuilder();

            int read;

            while ((read = input.read()) != - 1 ){

                sb.append((char) read);
            }

            String [] lines = sb.toString().split(System.lineSeparator());
            sb.delete(0, sb.length());
            for ( String el : lines){

                Integer x = Integer.parseInt(el);
                if ( x % 2 == 0 ){
                    sb.append(x).append(" - Четное");
                    sb.append(System.lineSeparator());
                }else {
                    sb.append(x).append(System.lineSeparator());
                }

            }

            System.out.println(sb.toString());

        } catch ( IOException e){
            e.printStackTrace();
        }

    }

}
