package streamApi;

import org.apache.commons.math3.analysis.function.Add;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Profiles {

    public static List<Address> collect (List<Profile> profiles){

        return profiles.stream()
                .map(address -> address.getAddress())
                .collect(Collectors.toList());
    }


}
