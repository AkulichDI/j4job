package iterator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CyclicIterator<T> implements Iterator<T> {

    private List<T> data;
    private int index = 0;
        /* здесь разместите поля класса, если они будут нужны  */

    public CyclicIterator(List<T> data) {
        this.data = data;
    }

    @Override
    public boolean hasNext() {
       if ( !data.isEmpty())return true;
       return false;
    }

    @Override
    public T next() {
        if ( !hasNext() ) throw new NoSuchElementException();
        if ( index == data.size()){
            index = 0;
        }
        return data.get(index++);
    }
}
