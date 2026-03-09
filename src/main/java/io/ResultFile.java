package io;

import java.io.*;
import java.util.Arrays;

public class ResultFile {


    public static String table ( int size){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sb.append((i + 1) * (j + 1));
                if (j + 1 == size) {
                    sb.append(System.lineSeparator());
                } else {
                    sb.append("  ");
                }
            }

        }
        return sb.toString();
    }



    public static void main(String[] args) {

        /*try (FileOutputStream output = new FileOutputStream("data/dataResult.txt")) {
            output.write("Hello world".getBytes());
            output.write(System.lineSeparator().getBytes());
            System.out.println(output.toString());

        }catch (IOException e ){
            e.printStackTrace();
        }

         */





/*
        try (FileOutputStream output = new FileOutputStream("data/dataHomeWork1.txt")) {
            output.write(table(9).getBytes());
            output.write(System.lineSeparator().getBytes());

        }catch (IOException e){
            e.printStackTrace();
        }

        try (FileInputStream input = new FileInputStream("data/dataHomeWork1.txt")){

            StringBuilder sb = new StringBuilder();
            sb.append(input.readAllBytes().toString());
            System.out.println(sb.toString());
            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
 */

        try (PrintWriter output = new PrintWriter(
                new BufferedOutputStream(
                     new FileOutputStream("data/dataresult.txt")
                ))) {
            output.println("Hello, world!");
            output.printf("%s%n", "Some string");
            output.printf("%d%n", 10);
            output.printf("%f%n", 1.5f);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}
