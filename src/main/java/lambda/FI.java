package lambda;

import java.util.Comparator;

public class FI {

    public static void main(String[] args) {
        Attachment[] attachments = {
                new Attachment("image 1" ,20),
                new Attachment("image 3" ,120),
                new Attachment("image 2" ,23)
        };
        Comparator<Attachment> comparator = new Comparator<Attachment>() {
            @Override
            public int compare(Attachment o1, Attachment o2) {
                return 0;
            }
        };
    }



}
