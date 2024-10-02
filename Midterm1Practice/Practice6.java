package Midterm1Practice;

import java.io.IOException;
import java.net.ServerSocket;

public class Practice6{
    public static void main(String[] args) throws IOException {
      
      //What is the output?
      String word = "hello";
      System.out.println(word.split("")[2]);
    }
  }
  //remember, split makes an array and the bracket retrieves the value at a specific index
  //answer: l