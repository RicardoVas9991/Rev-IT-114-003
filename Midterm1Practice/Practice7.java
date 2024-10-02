package Midterm1Practice;

import java.io.IOException;
import java.net.ServerSocket;

public class Practice7{
    public static void main(String[] args) throws IOException {
      
      //What is the output?
      String word = "hello";
      System.out.println(String.join(",", word.split("l")));
    }
  }
  //hint: the delimiter is bascially replaced with , in most cases
  //split will return the left and the right of the delimiter
  //answer: he,,o