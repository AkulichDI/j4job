package OperatorVetvlenia;
// 4 Задание
public class Game {
    public static void menu(String name) {
        if ("super mario".equals(name)) {
            System.out.println("Start - super mario");
        }
        if ("tanks".equals(name)) {
            System.out.println("Start - tanks");
        }
        if ("tetris".equals(name)) {
            System.out.println("Start - tetris");
        }
    }

    public static void main(String[] args) {
        Game.menu("tanks");
    }
}
