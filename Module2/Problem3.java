package Module2; // Important: the package corresponds to the folder it resides in
import java.util.Arrays;

// usage
// compile: javac Module2/Problem3.java
// run: java Module2.Problem3

public class Problem3 {
    public static void main(String[] args) {
        //Don't edit anything here
        Integer[] a1 = new Integer[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
        Integer[] a2 = new Integer[]{-1, 1, -2, 2, 3, -3, -4, 5};
        Double[] a3 = new Double[]{-0.01, -0.0001, -.15};
        String[] a4 = new String[]{"-1", "2", "-3", "4", "-5", "5", "-6", "6", "-7", "7"};
        
        bePositive(a1);
        bePositive(a2);
        bePositive(a3);
        bePositive(a4);
    }
    // <T> turns this into a generic so it can take in any datatype, it'll be passed as an Object so casting is required
    static <T> void bePositive(T[] arr){
        System.out.println("Processing Array:" + Arrays.toString(arr));
        Object[] output = new Object[arr.length];
        
        // rev - 9/21/2024
        for (int i = 0; i < arr.length; i++) {
            T value = arr[i];

            if (value instanceof Integer) {
                output[i] = Math.abs((Integer) value);
            } else if (value instanceof Double) {
                output[i] = Math.abs((Double) value);
            } else if (value instanceof String) {
                String strVal = (String) value;
                try {
                    int intValue = Integer.parseInt(strVal);
                    output[i] = String.valueOf(Math.abs(intValue));
                } catch (NumberFormatException e) {
                    output[i] = strVal;
                }
            } else {
                output[i] = value;
            }
        }

        StringBuilder sb = new StringBuilder();
        for(Object i : output){
            if(sb.length() > 0){
                sb.append(",");
            }
            sb.append(String.format("%s (%s)", i, i.getClass().getSimpleName().substring(0,1)));
        }
        System.out.println("Result: " + sb.toString());
    }
}