package collections;

import java.util.HashSet;
import java.util.List;

public class NotifyAccount {
    public static HashSet<Account> sent(List<Account> accounts) {

        HashSet<Account> result = new HashSet<>();
        for (Account acc : accounts){
            result.add(acc);
        }
        /* for-each accounts to HashSet; */
        return result;
    }
}
