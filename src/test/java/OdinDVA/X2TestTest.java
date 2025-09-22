package OdinDVA;
import org.junit.jupiter.api.Test;
import testirovanie.X2Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class X2TestTest {

    @org.junit.jupiter.api.Test
    void calc1() {
        int a = 10;
        int b = 0;
        int c = 0;
        int x = 2;
        int expected = 40;
        int result = X2Test.calc(a, b, c, x);
        assertThat(result).isEqualTo(expected);


    }

    @Test
    void calc2() {

      int   a = 1;
      int  b = 1;
      int  c = 1;
      int  x = 1;
      int  expected = 3;
      int  result = X2Test.calc(a, b, c, x);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void calc3() {

        int  a = 0;
        int  b = 1;
        int  c = 1;
        int  x = 1;
        int  expected = 2;
        int  result = X2Test.calc(a, b, c, x);
        assertThat(result).isEqualTo(expected);
    }


    @Test
    void calc4() {
        int  a = 1;
        int  b = 1;
        int  c = 0;
        int  x = 1;
        int  expected = 2;
        int  result = X2Test.calc(a, b, c, x);
        assertThat(result).isEqualTo(expected);
    }



    @Test
    void calc5() {
        int  a = 1;
        int  b = 1;
        int  c = 1;
        int  x = 0;
        int  expected = 1;
        int  result = X2Test.calc(a, b, c, x);
        assertThat(result).isEqualTo(expected);
    }

}