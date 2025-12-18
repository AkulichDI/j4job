package streamApi;

import java.util.Objects;

public class Product {
    private String name;
    private float price;
    private int standard;
    private int actual;

    public Product(String name, float price, int standard, int actual){
        this.name = name;
        this.price = price;
        this.standard = standard;
        this.actual = actual;
    }


    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public int getActual() {
        return actual;
    }

    public int getStandard() {
        return standard;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;

        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        Product product = (Product) obj;
        return Float.compare(product.price, price) == 0
                && standard == product.standard
                && actual == product.actual
                && Objects.equals(name, product.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, standard, actual);
    }
}
