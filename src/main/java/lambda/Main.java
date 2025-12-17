package lambda;




public class Main{

    public static void main(String[] args) {


        UserFactory factory = User::new;
        User user1 =  factory.create("Oleg");
        System.out.println(user1.getName());

        UserFactory2 factory2 = User::new;
        User user2 = factory2.create("Dima", 99);
        System.out.println(user2.getName() + " " + user2.getAge());

    }

}
