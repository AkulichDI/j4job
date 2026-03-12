package ru.job4j.condition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.withPrecision;
import static org.junit.jupiter.api.Assertions.*;

class PointTest {


    @Test
    void when00To20Then2(){
        double expected = 2;
        int x1 = 0;
        int y1 = 0;
        int x2 = 2;
        int y2 = 0;

        double output = Point.distance(x1,y1, x2, y2);
        assertThat(output).isEqualTo(expected, withPrecision(0.01));

    }

    @Test
    void when100To20Then80(){
        double expected = 8;
        int x1 = 10;
        int y1 = 0;
        int x2 = 2;
        int y2 = 0;
        double output = Point.distance(x1,y1,x2,y2);
        assertThat(expected).isEqualTo(output, withPrecision(0.01));

    }
    @Test
    void when10Minus20to20Then21Dot54(){
        double expected = 21.5406d;
        int x1 = 10;
        int y1 = -20;
        int x2 = 2;
        int y2 = 0;

        double  output = Point.distance(x1,y1,x2,y2);
        assertThat(expected).isEqualTo(output, withPrecision(0.001));
    }



}