package collections;

import java.util.Deque;

public class ReconstructPhrase {
    private final Deque<Character> descendingElements;

    private final Deque<Character> evenElements;

    public ReconstructPhrase(Deque<Character> descendingElements, Deque<Character> evenElements) {
        this.descendingElements = descendingElements;
        this.evenElements = evenElements;
    }
    private String getEvenElements() {
        StringBuilder tmp = new StringBuilder();
        int n = evenElements.size();
        for (int i = 0; i < n; i++) {
            if (i == 0 || i % 2 == 0 ){
                tmp.append(evenElements.poll());
            }else {
                evenElements.poll();
            }
        }
        return tmp.toString();
    }
    private String getDescendingElements() {
        StringBuilder tmp = new StringBuilder();
        int n = descendingElements.size();
        for (int i = 0; i < n; i++) {
            tmp.append(descendingElements.pollLast());
        }
        return tmp.toString();
    }
    public String getReconstructPhrase() {
        return getEvenElements() + getDescendingElements();
    }
}
