package streamApi;

public class Car  implements Comparable<Car>{
    private int year;
    private String model;
    private int maxSpeed;

    public Car (int year, String  model, int maxSpeed){
        this.year = year;
        this.model = model;
        this.maxSpeed = maxSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    @Override
    public int compareTo(Car o) {

        return this.year - o.year;
    }


    @Override
    public String toString() {
        return "\n\nCar:" +
                "\nyear=" + year +
                "\nmodel=" + model +
                "\nmaxSpeed=" + maxSpeed ;
    }
}
