package set;

import collections.SimpleArrayList;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;

public class SimpleArraySet<T> implements SimpleSet<T> {


    private SimpleArrayList<T> set = new SimpleArrayList<>(0);



    @Override
    public boolean add(T value) {

        if (validatorDataIfContainsTrue(value)) return false;

        set.add(value);

        return true;
    }

    @Override
    public boolean contains(T value) {

        if ( validatorDataIfContainsTrue(value) ) return true;
        return false;
    }

    @Override
    public Iterator<T> iterator()  {
        return set.iterator();
    }

    private boolean validatorDataIfContainsTrue(T value){
        Iterator<T> it = iterator();
        while (it.hasNext()){
            if (Objects.equals(it.next(),value)){
                return true;
            }
        }
        return false;
    }
}
