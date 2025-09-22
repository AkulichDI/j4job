package testirovanie;

import OperatorVetvlenia.*;
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
class DummyBotTest{
    @Test
    void whenGreetBot() {
        String input = "Hi, Bot.";
        String result = DummyBot.answer(input);
        String expected = "Hi, SmartAss.";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenByeBot() {
        String input = "Bye.";
        String result = DummyBot.answer(input);
        String expected = "See you later.";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenUnknownBot() {
        String input = "Can you add two plus two?";
        String result = DummyBot.answer(input);
        String expected = "I don't know. Please, ask another question.";
        assertThat(result).isEqualTo(expected);
    }




}
class MaxTest{
    @Test
    void whenMax1To2Then2() {
        int left = 1;
        int right = 2;
        int result = Max.max(left, right);
        int expected = 2;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenMaxMinus1To2Then2() {
        int left = -1;
        int right = 2;
        int result = Max.max(left, right);
        int expected = 2;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenMax200ToMinus4Then2() {
        int left = 200;
        int right = 3;
        int result = Max.max(left, right);
        int expected = 200;
        assertThat(result).isEqualTo(expected);
    }
    @Test
    void whenMax2To2Then2() {
        int left = 200;
        int right = 3;
        int result = Max.max(left, right);
        int expected = 200;
        assertThat(result).isEqualTo(expected);
    }


}
class  LogicNotTest{

    @Test
    void whenIsEvenTrue() {
        int num = 2;
        boolean result = LogicNot.isEven(num);
        assertThat(result).isTrue();
    }

    @Test
    void whenIsEvenFalse() {
        int num = 3;
        boolean result = LogicNot.isEven(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenIsPositiveTrue() {
        int num = 2;
        boolean result = LogicNot.isPositive(num);
        assertThat(result).isTrue();
    }

    @Test
    void whenIsPositiveFalse() {
        int num = -2;
        boolean result = LogicNot.isPositive(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenNumIs0IsPositiveFalse() {
        int num = 0;
        boolean result = LogicNot.isPositive(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenNotEvenFalse() {
        int num = 2;
        boolean result = LogicNot.notEven(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenNotEvenTrue() {
        int num = 3;
        boolean result = LogicNot.notEven(num);
        assertThat(result).isTrue();
    }

    @Test
    void whenNegativeTrue() {
        int num = -2;
        boolean result = LogicNot.isNegative(num);
        assertThat(result).isTrue();
    }

    @Test
    void whenNegativeFalse() {
        int num = 2;
        boolean result = LogicNot.isNegative(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenNumIs0NegativeFalse() {
        int num = 0;
        boolean result = LogicNot.isNegative(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenNotEvenAndPositiveIsTrue() {
        int num = 3;
        boolean result = LogicNot.notEvenAndPositive(num);
        assertThat(result).isTrue();
    }

    @Test
    void whenNotEvenFalseIsAllFalse() {
        int num = 2;
        boolean result = LogicNot.notEvenAndPositive(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenPositiveFalseIsAllFalse() {
        int num = -3;
        boolean result = LogicNot.notEvenAndPositive(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenNotEvenAndPositiveIsFalse() {
        int num = 0;
        boolean result = LogicNot.notEvenAndPositive(num);
        assertThat(result).isFalse();
    }

    @Test
    void whenEvenOrNegativeIsTrue() {
        int num = -2;
        boolean result = LogicNot.evenOrNegative(num);
        assertThat(result).isTrue();
    }

    @Test
    void whenEvenIsTrueThenAllIsTrue() {
        int num = 2;
        boolean result = LogicNot.evenOrNegative(num);
        assertThat(result).isTrue();
    }

    @Test
    void whenNegativeIsTrueThenAllIsTrue() {
        int num = -3;
        boolean result = LogicNot.evenOrNegative(num);
        assertThat(result).isTrue();
    }

    @Test
    void whenEvenOrNegativeIsFalse() {
        int num = 3;
        boolean result = LogicNot.evenOrNegative(num);
        assertThat(result).isFalse();
    }

}

class TriangleTest {
        @Test
        void whenExistTrue() {
            double ab = 2.0;
            double ac = 2.0;
            double bc = 2.0;
            boolean result = Triangle.exist(ab, ac, bc);
            assertThat(result).isTrue();
        }
        @Test
        void  whenNotExist() {
            double ab = -800;
            double ac = 90;
            double bc = 90;
            boolean result = Triangle.exist(ab, ac, bc);
            assertThat(result).isFalse();
        }



}



class ChessBoardTest {
    @Test
    public void wayIs5() {
        int x1 = 6;
        int y1 = 7;
        int x2 = 1;
        int y2 = 2;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 5;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void wayIs7() {
        int x1 = 7;
        int y1 = 0;
        int x2 = 0;
        int y2 = 7;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 7;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void wayIs0() {
        int x1 = 2;
        int y1 = 6;
        int x2 = 4;
        int y2 = 1;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenX1isMinus1ThenWayIs0() {
        int x1 = -1;
        int y1 = 6;
        int x2 = 4;
        int y2 = 1;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenY1isMinus1ThenWayIs0() {
        int x1 = 2;
        int y1 = -1;
        int x2 = 4;
        int y2 = 1;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenX2isMinus1ThenWayIs0() {
        int x1 = 2;
        int y1 = 6;
        int x2 = -1;
        int y2 = 1;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenY2isMinus1ThenWayIs0() {
        int x1 = 2;
        int y1 = 6;
        int x2 = 4;
        int y2 = -1;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenX1isGreater7ThenWayIs0() {
        int x1 = 10;
        int y1 = 6;
        int x2 = 4;
        int y2 = 1;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenY1isGreater7ThenWayIs0() {
        int x1 = 2;
        int y1 = 10;
        int x2 = 4;
        int y2 = 1;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenX2isGreater7ThenWayIs0() {
        int x1 = 2;
        int y1 = 6;
        int x2 = 10;
        int y2 = 1;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenY2isGreater7ThenWayIs0() {
        int x1 = 2;
        int y1 = 6;
        int x2 = 4;
        int y2 = 10;
        int result = ChessBoard.way(x1, y1, x2, y2);
        int expected = 0;
        assertThat(result).isEqualTo(expected);
    }
}

class DivideBySixTest {
    @Test
    void whenNumberDivideBy6() {
        int input = 24;
        String result = DivideBySix.checkNumber(input);
        String expected = "The number divides by 6.";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenNumberDivideBy3AndNotEven() {
        int input = 9;
        String result = DivideBySix.checkNumber(input);
        String expected = "The number divides by 3, but it isn't the even number.";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenNumberNoDivideBy3AndEven() {
        int input = 14;
        String result = DivideBySix.checkNumber(input);
        String expected = "The number doesn't divide by 3, but it is the even number.";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenNumberNoDivideBy3AndNotEven() {
        int input = 25;
        String result = DivideBySix.checkNumber(input);
        String expected = "The number doesn't divide by 3 and it isn't the even number.";
        assertThat(result).isEqualTo(expected);
    }
}
class ThreeMaxTest {
    @Test
    void firstMax() {
        int first = 10;
        int second = 5;
        int third = 1;
        int result = ThreeMax.max(first, second, third);
        int expected = 10;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void secondMax() {
        int first = 10;
        int second = 50;
        int third = 1;
        int result = ThreeMax.max(first, second, third);
        int expected = 50;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void thirdMax() {
        int first = 1;
        int second = 5;
        int third = 100;
        int result = ThreeMax.max(first, second, third);
        int expected = 100;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void allEq() {
        int first = 1;
        int second = 1;
        int third = 1;
        int result = ThreeMax.max(first, second, third);
        int expected = 1;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void firstEqSecond() {
        int first = 10;
        int second = 10;
        int third = 1;
        int result = ThreeMax.max(first, second, third);
        int expected = 10;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void firstEqThird() {
        int first = 100;
        int second = 1;
        int third = 100;
        int result = ThreeMax.max(first, second, third);
        int expected = 100;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void secondEqThird() {
        int first = 1;
        int second = 100;
        int third = 100;
        int result = ThreeMax.max(first, second, third);
        int expected = 100;
        assertThat(result).isEqualTo(expected);
    }
}