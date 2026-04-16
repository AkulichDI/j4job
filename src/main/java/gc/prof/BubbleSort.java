package gc.prof;

public class BubbleSort implements Sort {
    @Override
    public boolean sort(Data data) {
        int[] array = data.getClone();
        return true;
    }



    public void sort (int[] array ){

        int out, in;
        for (out = array.length - 1; out >= 1; out--){
            for (in = 0; in < out; in++){

                if (array[in] > array[in + 1]){
                    swap(array, in, in + 1 );
                }

            }

        }

    }

    public void swap(int[] array, int i, int j){

        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;


    }



}
