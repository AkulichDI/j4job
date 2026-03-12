package ru.job4j.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;
import static org.junit.jupiter.api.Assertions.*;

class X2Test {


    @Test
    void whenA10B0C0X2Then30(){
        // Вход
        int a = 10;
        int b = 0;
        int c = 0;
        int x = 2;
        // Ожидаемое значение
        int expected = 40;
        // Вызов метода для получения результата
        int result = X2.calc(a,b,c,x);
        assertEquals(expected, result);
    }


    @Test
    void whenA1B1C1X1Then3(){
        int a = 1;
        int b = 1;
        int c = 1;
        int x = 1;

        int expected = 3;
        int result = X2.calc(a,b,c,x);
        assertEquals(expected, result);
    }


    @Test
    void whenA0B1C1X1Then2(){

        int a = 0;
        int b = 1;
        int c = 1;
        int x = 1;
        int expected = 2;
        int result = X2.calc(a,b,c,x);
        assertEquals(expected, result);

    }

    @Test
    void whenA1B1C0X1(){
        int a = 1;
        int b = 1;
        int c = 0;
        int x = 1;
        int expected = 2;
        int result = X2.calc(a,b,c,x);
        assertEquals(expected, result);
        System.out.println(expected + " : " + result );
    }







}