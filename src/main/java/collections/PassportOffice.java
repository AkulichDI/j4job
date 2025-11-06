package collections;

import java.util.*;

public class PassportOffice {
    private Map<String, Citizen> citizens = new HashMap<>();

    public boolean add(Citizen citizen) {
        boolean result = false;
        if (!citizens.containsKey(citizen.getPassport()) ) {
             citizens.put(citizen.getPassport(), citizen);
           result = true;
        }
        return result;
    }
    public Citizen get(String passport) {
        return citizens.get(passport);
    }
}
