package collections;

import java.util.HashMap;

public class UsageMap {

    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        map.put("akulich@hx.com", "FIO1");
        map.put("a@hx.com", "FIO2");
        map.put("ak@hx.com", "FIO");
        map.put("akul@hx.com", "FIO");
        map.put("akuli@hx.com", "FIO");
        for (String key : map.keySet()){
            String value = map.get(key);
            System.out.println("Ключ: " + key+ ":  Значение: " +value);
        }
    }


}
