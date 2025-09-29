package OperatorVetvlenia;
// 6 задание
public class Max {

    public static int max(int left, int right) {
        int result = left > right? left: right;
        return result;
    }

    public static  int max(int fs,int sc, int th, int fo){


        return max(fs,sc) > max(th,fo)? max(fs,sc): max(th,fo);
    }

    public static  int max(int fs,int sc, int th){


        return max(fs,sc) > th? max(fs,sc): th;
    }

    public static int summation(int first, int second) {
        int result = first + second;
        return result;
    }

    public static void main(String[] args) {

        int left = 4;
        int rright = 3;
        int max = Max.max(left, rright);
        int sum = Max.summation(left, rright);

        System.out.println(max);
         max = Max.max(10,20,30, rright);
        System.out.println(max);
        max = Max.max(1000,200,20);
        System.out.println(max);
    }


}
