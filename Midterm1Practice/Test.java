package Midterm1Practice;

import java.io.IOException;
import java.net.ServerSocket;

public class Test{
    public static String nums(String n){
        if(n.length() > 0){
            n = n.substring(1);
            System.out.println("Sub: " + n);
            String s = nums(n);
            System.out.println("Returned: " + s);
            return s;

        }
        return "";
    }
    public static void main(String[] args) throws IOException {
        String word = "hello";
        //System.out.println(String.join(",",word.split("")));
        //System.out.println(word.substring(word.length()-2));
        //System.out.println(word.substring(1,3));
        //System.out.println(word.split("")[2]);
        //System.out.println(String.join(",", word.split("l")));

        //listen example
        //ServerSocket server = new ServerSocket(3000);
        //ServerSocket server2 = new ServerSocket(3000);

        System.out.println(nums("duck"));
       /* int a = 10;
        for(int i = 0; i < 10; i++){
            System.out.println(i/2);
            if(i/2 == 0){
                a--;
            }
        }
        System.out.println("Answer:");
        System.out.println(a);*/

    }
}
