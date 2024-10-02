package Midterm1Practice;

import java.io.IOException;
import java.net.ServerSocket;

public class Practice4{
    public static void main(String[] args) throws IOException {
      
      //What is the output?
      String word = "hello";
      System.out.println(word.substring(word.length()-2));
    }
  }
  //length of 5 - 2 = 3; substring will read from index 3 until the end of the String
  //anser: lo