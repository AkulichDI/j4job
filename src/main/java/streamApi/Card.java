package streamApi;


import org.apache.poi.ss.formula.functions.T;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

public class Card {
    private  Suit suit;
    private  Value value;

    public Card(Suit suit, Value value){
        this.suit = suit;
        this.value = value;
    }

    public LinkedHashMap<T, T > createCard (List<T> ar1, List<T> ar2){
        return      new LinkedHashMap<>();
    }
    public enum Value {
        V_6, V_7, V_8
    }

    public enum Suit {
        Diamonds, Hearts, Spades, Clubs
    }

    public static void main(String[] args) {
        Stream.of(Suit.values())
                .flatMap(suit ->
        Stream.of(Value.values())
                 .map(value -> suit + " " + value))
        .forEach(System.out::println);
    }





}



