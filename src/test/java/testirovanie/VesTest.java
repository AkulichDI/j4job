package testirovanie;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.BDDAssertions.withPrecision;

class VesTest {

    @Test
    void whenMan180then92() {
        short input = 180;
        double exepected = 92;
        double output = Ves.manWeight(input);
        assertThat(output).isEqualTo(exepected, withPrecision(0.01));
    }
    @Test
    void whenWoman170Then92(){
        short input = 179;
        double exepected = 90.85;
        double output = Ves.womanWeight(input);
        assertThat(output).isEqualTo(exepected, withPrecision(0.01));
    }


}
class PointTest {
    @Test
    void when00to20then2() {
        double expected = 2;
        int x1 = 0;
        int y1 = 0;
        int x2 = 2;
        int y2 = 0;
        double output = Point.distance(x1, y1, x2, y2);
        assertThat(output).isEqualTo(expected, withPrecision(0.01));
    }

    @Test
    void when0034to2then2() {
        double expected = 5;
        int x1 = 0;
        int y1 = 0;
        int x2 = 3;
        int y2 = 4;
        double output = Point.distance(x1, y1, x2, y2);
        assertThat(output).isEqualTo(expected, withPrecision(0.01));
    }

    @Test
    void when5534to2then2() {
        double expected = 2.23;
        int x1 = 5;
        int y1 = 5;
        int x2 = 3;
        int y2 = 4;
        double output = Point.distance(x1, y1, x2, y2);
        assertThat(output).isEqualTo(expected, withPrecision(0.01));
    }

    @Test
    void when100202020to2then2() {
        double expected = 80;
        int x1 = 100;
        int y1 = 20;
        int x2 = 20;
        int y2 = 20;
        double output = Point.distance(x1, y1, x2, y2);
        assertThat(output).isEqualTo(expected, withPrecision(0.01));
    }


}
class RectangleAreaTest{
    @Test
    void whenP6K2Square2() {
        int expected = 2;
        int p = 6;
        double k = 2;
        double output = RectangleArea.square(p, k);
        assertThat(output).isEqualTo(expected, withPrecision(0.01));
    }


    @Test
    void whenPMinus2KMinusInfinitySquare2() {
      //  String expected = ;
        int p = -2;
        double k = -1;
        double output = RectangleArea.square(p, k);
        assertThat(output).isEqualTo(Double.NEGATIVE_INFINITY);
    }
    @Test
    void whenMinus2K476Square2() {
        double expected = 0;
        int p = -2;
        double k = 476;
        double output = RectangleArea.square(p, k);
        assertThat(output).isEqualTo(expected, withPrecision(0.01));
    }

}