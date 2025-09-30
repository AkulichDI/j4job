public class mestizoTest {

    private int[] array;

    public void to(int[] array) {

        this.array = array;
    }

    public void print() {
        for (int i : this.array) {
            System.out.println(i);
        }
    }
    public static void main(String[] args) {
        int[] array = {1, 2, 3};
        mestizoTest reduce = new mestizoTest();
        reduce.to(array);
        reduce.print();
    }

}
