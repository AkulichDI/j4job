package collections;

import java.util.Queue;

public class AppleStore {
    private final Queue<Customer> queue;

    private final int count;

    public AppleStore(Queue<Customer> queue, int count) {
        this.queue = queue;
        this.count = count;
    }
    public String getLastHappyCustomer() {
        return "";
    }
    public String getFirstUpsetCustomer() {
        return "";
    }
}
