package Midterm1Practice;

import java.io.IOException;
import java.net.ServerSocket;

public class Practice5{
    public static void main(String[] args) throws IOException {
      
      //What is the output?
      String word = "hello";
      System.out.println(word.substring(1,3));
    }
  }
  //end index is exclusive so it'll read index 1-2
  //answer: el