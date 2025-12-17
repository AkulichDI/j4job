package lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FunctionCalculator {

    public List<Double> diapason(int start, int end, Function<Double,Double> function){
        List<Double> rslt = new ArrayList<>();
        for (double i = start; i<end; i++){
            rslt.add(function.apply(i));
        }
        return rslt;
    };


}
