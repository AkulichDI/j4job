package io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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




    }

}
