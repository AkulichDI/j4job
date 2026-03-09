package io;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LogFilter {

    private final String file;


    public LogFilter(String file) {
        this.file = file;
    }


    public List<String> filter(){
        List<String> data = new ArrayList<>();

        try ( BufferedReader dataIn = new BufferedReader(new FileReader(this.file))) {

            data = dataIn.lines().filter(s -> s.contains(" 404 ")).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    public void saveTo(String out){
        var data = filter();

        try (PrintWriter outData = new PrintWriter(new BufferedOutputStream(new FileOutputStream(out)))){

            data.forEach(outData::println);


        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new LogFilter("data/log.txt").saveTo("data/404.txt");
    }
}
