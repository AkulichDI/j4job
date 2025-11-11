package bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankService {
    private final Map<User, List<Account>> users = new HashMap<>();

    public void addUser(User user) {
        users.putIfAbsent(user, new ArrayList<Account>());
    }
    public void deleteUser(String passport) {
        users.remove(findByPassport(passport));
    }
    public void addAccount(String passport, Account account) {
       if (findByPassport(passport) != null ) {
          List<Account> accounts =  users.get(findByPassport(passport));
           accounts.add(account);
       }
    }
    public User findByPassport(String passport) {
        for (User user : users.keySet()){
            if (user.getPassport().equals(passport)){
                return user;
            }
        }
        return null;
    }

    public Account findByRequisite(String passport, String requisite) {
        if(findByPassport(passport) != null){
            List<Account> accounts = users.get(findByPassport(passport));
            for (Account account : accounts){
                if ( account.getRequisite().equals(requisite) )
                    return account;

            }
        }

        return null;
    }
    public boolean transferMoney(String sourcePassport, String sourceRequisite,
                                 String destinationPassport, String destinationRequisite,
                                 double amount) {
        boolean result = false;
        if ( findByPassport(sourcePassport) != null && findByPassport(destinationPassport) != null ) {
            List<Account> accountsSource = users.get(findByPassport(sourcePassport));
            List<Account> accountsDestination  = users.get(findByPassport(destinationPassport));
            for (Account account : accountsSource){
                if (account.getRequisite().equals(sourceRequisite) && account.getBalance() >= amount){
                        account.setBalance(account.getBalance() - amount);
                        break;
                }
            }
            for (Account account : accountsDestination){
                if (account.getRequisite().equals(sourceRequisite) ){
                    account.setBalance(account.getBalance() + amount);
                    break;
                }
            }

            result = true;
        }

        return result;
    }
    public List<Account> getAccounts(User user) {
        return users.get(user);
    }

}
