package collections;

public interface SimpleList<T> extends Iterable {

    void add( T value );
    T set ( int index, T newValue);
    T remove ( int index );
    T get ( int index);
    int size();



}
