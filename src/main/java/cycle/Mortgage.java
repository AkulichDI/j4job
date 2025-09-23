package cycle;



public class Mortgage {

    public static int year(double amount, int salary, double percent) {
        int year = 0;

        while (amount>=0){

            amount -= salary;
            amount *= (percent/ 100);
            year++;

        }


        return year;
    }

}
