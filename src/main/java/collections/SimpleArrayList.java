package collections;

import java.util.*;

public class SimpleArrayList<T> implements SimpleList<T> {

    private int size;
    private int modCount;
    private T[] array;

    public SimpleArrayList(int i) {
        array = (T[]) new Object[i];
    }

    @Override
    public void add(T value) {
        ensureCapacity(size + 1);
        array[size++] = value;
        modCount++;
    }

    @Override
    public T set(int index, T newValue) {
        checkIndex(index);
        T old = (T) array[index];
        array[index] = newValue;
        return old;
    }

    @Override
    public T remove(int index) {
        checkIndex(index);
        T old = (T) array[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(array, index + 1, array, index, numMoved);
        }
        array[--size] = null;
        modCount++;
        return old;
    }

    @Override
    public T get(int index) {
        checkIndex(index);
        return (T) array[index];
    }

    @Override
    public int size() {
        return size;
    }

    private void ensureCapacity(int minCapacity) {
        if (array.length < minCapacity) {
            int newCap = array.length == 0 ? 1 : array.length * 2;
            if (newCap < minCapacity) newCap = minCapacity;
            array = Arrays.copyOf(array, newCap);
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index=" + index + ", size=" + size);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private final int expectedModCount = modCount;
            private int cursor;

            private void checkForComodification() {
                if (expectedModCount != modCount) {
                    throw new ConcurrentModificationException();
                }
            }

            @Override
            public boolean hasNext() {
                checkForComodification();
                return cursor < size;
            }

            @Override
            public T next() {
                checkForComodification();
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return array[cursor++];
            }
        };
    }
}

