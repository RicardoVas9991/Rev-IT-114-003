package Midterm1Practice;

import java.io.IOException;
import java.net.ServerSocket;

public class Practice3{
    public static void main(String[] args) throws IOException {
      
      //What is the output?
      String word = "hello";
      System.out.println(String.join(",",word.split("")));
    }
  }
  //split on empty string breaks up all the characters of a String