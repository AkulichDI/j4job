package streamApi;

import java.time.LocalDate;

public class CarLs2 {

    private String brand;
    private String model;
    private LocalDate created;
    private double volume;
    private String color;




    static class Builder {

        private String brand;
        private String model;
        private LocalDate created;
        private double volume;
        private String color;


        Builder buildBrand(String brand) {
            this.brand = brand;
            return this;
        }

        Builder buildModel(String model) {
            this.model = model;
            return this;
        }

        Builder buildCreated(LocalDate created) {
            this.created = created;
            return  this;
        }

        Builder buildVolume(double volume) {
            this.volume = volume;
            return this;
        }

        Builder buildColor(String color) {
            this.color = color;
            return this;
        }
        CarLs2 build() {
            CarLs2 car = new CarLs2();
            car.brand = brand;
            car.model = model;
            car.created = created;
            car.volume = volume;
            car.color = color;
            return car;
        }
    }



    public static void main(String[] args) {
        CarLs2 car = new Builder()
                .buildBrand("Toyota")
                .buildModel("Camry")
                .buildCreated(LocalDate.of(2021, 6, 1))
                .buildVolume(2.5)
                .buildColor("Red")
                .build();
        System.out.println(car.toString());

        CarLs2 car2 = new Builder()
                .buildBrand("Honda")
                .buildCreated(LocalDate.of(2020,1,1))
                .buildVolume(2.5)
                .buildColor("White")
                .build();
        System.out.println(car2.toString());
    }

    @Override
    public String toString() {
        return "CarLs2{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", created=" + created +
                ", volume=" + volume +
                ", color='" + color + '\'' +
                '}';
    }
}
