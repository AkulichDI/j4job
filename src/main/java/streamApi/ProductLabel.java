package streamApi;

import java.util.List;
import java.util.stream.Collectors;

public class ProductLabel {

    public List<String> generateLabels(List<Product> products){
        return products.stream()
                .filter(x -> x.getStandard() - x.getActual() <= 3 && x.getStandard() - x.getActual() >= 0)
                .map(x -> new Label(x.getName(), x.getPrice()/2))
                .map(Label::toString)
                .toList();
    }

    public static void main(String[] args) {

    }


}
