package iterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class NonNullIterator implements Iterator<Integer> {

    private Integer data[];
    private int index;



    public NonNullIterator(Integer[] data){
        this.data = data;
    }


    @Override
    public boolean hasNext() {
        while (index < data.length && data[index] == null) {
            index++;
        }
        return index < data.length;
    }

    @Override
    public Integer next() {

       if ( !hasNext()) throw new NoSuchElementException();
       return data[index++];
    }
}
