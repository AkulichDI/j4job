package iterator;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;

public class ListUtils {

    public static <T> void addBefore(List<T> list, int index, T value) {
        Objects.checkIndex(index, list.size());
        ListIterator<T> iterator = list.listIterator();
        while (iterator.hasNext()) {
            if (iterator.nextIndex() == index) {
                iterator.add(value);
                break;
            }
            iterator.next();
        }
    }

    public static <T> void addAfter(List<T> list, int index, T value) {
        Objects.checkIndex(index, list.size());
        ListIterator<T> it = list.listIterator();
        while (it.hasNext()) {
            if (it.nextIndex() == index) {
                it.next();
                it.add(value);
                break;
            }
            it.next();
        }
    }

    public static <T> void removeIf(List<T> list, Predicate<T> filter) {

        ListIterator<T> it = list.listIterator();
        while (it.hasNext()){
            if (filter.test(it.next())){
                it.remove();
            }
        }

    }

    public static <T> void replaceIf(List<T> list, Predicate<T> filter, T value) {

        ListIterator<T> it = list.listIterator();
        while (it.hasNext()){
            if (filter.test(it.next())){
                it.set(value);
            }
        }


    }

    public static <T> void removeAll(List<T> list, List<T> elements) {

        ListIterator<T> it = list.listIterator();

        while (it.hasNext()){
            if (elements.contains(it.next())) {
                it.remove();
            }
        }

    }



}
