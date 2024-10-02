package Midterm1Practice;

public class Practice8 {
    static int stack = 0;
    public static String nums(String n) {
        stack++;
        if (n.length() > 0) {
            n = n.substring(1);
            System.out.println("Pre-Stack: " + stack);
            System.out.println("Value: " + n);
            // recur
            String s = nums(n);
            stack--;
            System.out.println("Post-Stack: " + stack);
            System.out.println("Value: " + n);

            return n;

        }
        return "";
    }

    public static void main(String[] args) {
        // what's the output?
        nums("duck");

    }
}

//note stack is LIFO
//pay attention to the order of execution (pre vs post values depending on where the output is done)