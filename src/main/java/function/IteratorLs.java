package function;

import java.util.*;

public class IteratorLs {

    private int with;
    private int height;


    public IteratorLs(int with, int height) {
        this.with = with;
        this.height = height;
    }


    @Override
    public String toString() {
        return "IteratorLs{" +
                "with=" + with +
                ", height=" + height +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        IteratorLs that = (IteratorLs) object;
        return with == that.with && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(with, height);
    }

    public void setWith(int with) {
        this.with = with;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWith() {
        return with;
    }

    public int getHeight() {
        return height;
    }

    public static void main(String[] args) {

        List<IteratorLs> list = new ArrayList<>();

        list.add(new IteratorLs(10,20));
        list.add(new IteratorLs(10,0));
        list.add(new IteratorLs(0,20));
        list.add(new IteratorLs(20,20));
        list.add(new IteratorLs(30,20));
        list.add(new IteratorLs(40,20));


        Iterator<IteratorLs> it = list.iterator();

        while (it.hasNext()){

           IteratorLs iteratorLs =  it.next();

            System.out.println("Высота: " + iteratorLs.getHeight() + " Ширина: " + iteratorLs.getWith());
           if ( iteratorLs.getWith() > 10 ){
               System.out.println("Удаляем: " + iteratorLs.getWith() + "  и  " + iteratorLs.getHeight());
               it.remove();
           }
            System.out.println("Переходим на след итерацию ");
        }

        System.out.println(list.toString());

    }


}
