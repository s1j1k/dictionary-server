package org.example;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class DictionaryClient {

  private DictGUI gui;

  DictionaryClient() {
    this.gui = new DictGUI();
  }

  public static void main(String args[]) throws IOException {
    DictionaryClient dictionaryClient = new DictionaryClient();
    int port = 1234; //Integer.parseInt(args[1]);
    Socket clientSocket = new Socket("localhost", port); // new Socket("clouds.cis.unimelb.edu.au",port);
    DataOutputStream os = null;
    DataInputStream is = null; // note this is the opposite order as in server
    try {
      os = new DataOutputStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
      // Send a request in string format
      os.writeUTF("INIT");
      System.out.println("Client sent request: INIT");

      // read the response from the server
      String response = is.readUTF();
      System.out.println("Client received response: " + response);

      // initialize the dictionary in the GUI
      ArrayList<Word> words = new Gson()
        .fromJson(response, new TypeToken<List<Word>>() {}.getType());
      dictionaryClient.gui.setWords(words);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      // When done, just close the connection and exit
      os.close();
      clientSocket.close();
    }
  }
}
