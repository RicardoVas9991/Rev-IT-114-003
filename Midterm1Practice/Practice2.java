package Midterm1Practice;

import java.io.IOException;
import java.net.ServerSocket;

public class Practice2{
    public static void main(String[] args) throws IOException {
      
      //Can an appliction (or two applications) listen on the same port at the same time?
      ServerSocket server = new ServerSocket(3000);
      ServerSocket server2 = new ServerSocket(3000);
    }
  }
  //run to find out, note the exception