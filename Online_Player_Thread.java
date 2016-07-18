/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FLOG_Schema;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
/**
 *
 * @author Nandika
 */
public class Online_Player_Thread extends Thread {
  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final Online_Player_Thread[] threads;
  private int maxClientsCount;
  private int conntected_Players;
  int[] scoreArray ;


  public Online_Player_Thread(Socket clientSocket, Online_Player_Thread[] threads,int concted_plys) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
    this.conntected_Players=concted_plys;
    System.out.println("Connected Players: " +conntected_Players);

  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    Online_Player_Thread[] threads = this.threads;

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true) {
        os.println("Enter your name.");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("The name should not contain '@' character.");
        }
      }
            
      /* Welcome the new the client. */
      os.println("Welcome " + name
          + " to our chat room.\nTo leave enter /quit in a new line.");
         
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] == this) {
            clientName = "@" + name;
            break;
          }
        }
        
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null) {
            threads[i].os.println("*** A new user " + name
                + " entered the chat room !!! ***");
            threads[i].os.println("playerCount:" + conntected_Players);
            
               
          }
        }
      }
      /* Start the conversation. */
      while (true) {
        String line = is.readLine();
        if (line.startsWith("/quit")) {
          break;
        }
        /* If the message is private sent it to the given client. */
        if (line.startsWith("@")) {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                  if (threads[i] != null && threads[i] != this
                      && threads[i].clientName != null
                      && threads[i].clientName.equals(words[0])) {
                    threads[i].os.println("<" + name + "> " + words[1]);
                    /*
                     * Echo this message to let the client know the private
                     * message was sent.
                     */
                    this.os.println(">" + name + "> " + words[1]);
                    break;
                  }
                }
              }
            }
          }
        } else {
          /* The message is public, broadcast it to all other clients. */
          scoreArray=new int[3];
          synchronized (this) {
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null) {
                threads[i].os.println(line+":"+name);
                
                   
                //Get the scores and Sort
//                if(line.startsWith("scores")){
//                            //Get player Score and store in Map
//                           
//                            String score = line.split(":")[1];
//                           // scoreArray[0]=Integer.parseInt(score);
//                                scoreArray[0]=10;
//                                scoreArray[1]=221;
//                                scoreArray[2]=123;
//                                Arrays.sort(scoreArray);
//                              
//                                System.out.println(Arrays.toString(scoreArray));
//                                
//                                for (int j = 0; j < 3; j++) {
//                        System.out.println("Server Score check: " +scoreArray[j]);
//                               }
//                            
//                         
//                            
//                            
//                    }
                 //Get the scores and Sort End  
                
              }
            }
          }
        }
      }
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this
              && threads[i].clientName != null) {
            threads[i].os.println("*** The user " + name
                + " is leaving the chat room !!! ***");
          }
        }
      }
      os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  } 
}
