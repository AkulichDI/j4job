package ru.job4j.iterator;

import iterator.Balancer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalancerTest {
    @Test
    void whenSingleAndIteratorThree() {
        List<ArrayList<Integer>> nodes = List.of(
                new ArrayList<>()
        );
        Iterator<Integer> source = List.of(1, 2, 3).iterator();
        Balancer.split(nodes, source);
        assertThat(nodes).isEqualTo(
                List.of(
                        List.of(1, 2, 3)
                )
        );
    }

    @Test
    void whenThreeListsAndIteratorEmpty() {
        List<ArrayList<Integer>> nodes = List.of(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        Iterator<Integer> source = Collections.emptyIterator();
        Balancer.split(nodes, source);
        assertThat(nodes).containsExactlyInAnyOrder(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    @Test
    void whenTwoListsAndIteratorThree() {
        List<ArrayList<Integer>> nodes = List.of(
                new ArrayList<>(),
                new ArrayList<>()
        );
        Iterator<Integer> source = List.of(1, 2, 3).iterator();
        Balancer.split(nodes, source);
        assertThat(nodes).containsExactlyInAnyOrder(
                new ArrayList<>(List.of(1, 3)),
                new ArrayList<>(List.of(2))
        );
    }

    @Test
    void whenThreeListsAndIteratorThree() {
        List<ArrayList<Integer>> nodes = List.of(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        Iterator<Integer> source = List.of(1, 2, 3).iterator();
        Balancer.split(nodes, source);
        assertThat(nodes).containsExactlyInAnyOrder(
                new ArrayList<>(List.of(1)),
                new ArrayList<>(List.of(2)),
                new ArrayList<>(List.of(3))
        );
    }

    @Test
    void whenThreeListsAndIteratorTwo() {
        List<ArrayList<Integer>> nodes = List.of(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        Iterator<Integer> source = List.of(1, 2).iterator();
        Balancer.split(nodes, source);
        assertThat(nodes).containsExactlyInAnyOrder(
                new ArrayList<>(List.of(1)),
                new ArrayList<>(List.of(2)),
                new ArrayList<>()
        );
    }

    @Test
    void whenThreeListsAndIteratorFour() {
        List<ArrayList<Integer>> nodes = List.of(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        Iterator<Integer> source = List.of(1, 2, 3, 4).iterator();
        Balancer.split(nodes, source);
        assertThat(nodes).containsExactlyInAnyOrder(
                new ArrayList<>(List.of(1, 4)),
                new ArrayList<>(List.of(2)),
                new ArrayList<>(List.of(3))
        );
    }

    @Test
    void whenThreeListsAndIteratorFive() {
        List<ArrayList<Integer>> nodes = List.of(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        Iterator<Integer> source = List.of(1, 2, 3, 4, 5).iterator();
        Balancer.split(nodes, source);
        assertThat(nodes).containsExactlyInAnyOrder(
                new ArrayList<>(List.of(1, 4)),
                new ArrayList<>(List.of(2, 5)),
                new ArrayList<>(List.of(3))
        );
    }

    @Test
    void whenThreeListsAndIteratorSix() {
        List<ArrayList<Integer>> nodes = List.of(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        Iterator<Integer> source = List.of(1, 2, 3, 4, 5, 6).iterator();
        Balancer.split(nodes, source);
        assertThat(nodes).containsExactlyInAnyOrder(
                new ArrayList<>(List.of(1, 4)),
                new ArrayList<>(List.of(2, 5)),
                new ArrayList<>(List.of(3, 6))
        );
    }
}