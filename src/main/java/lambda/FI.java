package lambda;

import java.util.Arrays;
import java.util.Comparator;

public class FI {

    public static void main(String[] args) {
        Attachment[] attachments = {
                new Attachment("image 1" ,20),
                new Attachment("image 3" ,120),
                new Attachment("image 2" ,23)
        };

        Comparator<Attachment> comporatorText = (left , right ) -> left.getName().compareTo(right.getName());
        Comparator<Attachment> comporatorDesc = (left, right) -> {
            System.out.println("compare - " + left.getSize() + " : " + right.getSize());
            return Integer.compare(right.getSize(), left.getSize());
        };
       // Arrays.sort(attachments, comporatorText);
        Arrays.sort(attachments, comporatorDesc);


        for(Attachment obj : attachments){
            System.out.println("Имя: " + obj.getName()  + "\nSize: " + obj.getSize());
        }


    }



}
