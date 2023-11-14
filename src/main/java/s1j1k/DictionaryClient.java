package s1j1k;

import java.io.*;
import java.net.*;

public class DictionaryClient {
    //private DictGUI gui;
    DictionaryClient() {
        //this.gui = new DictGUI();
    }

    public static void main(String args[]) throws IOException {
        DictionaryClient dictionaryClient = new DictionaryClient();
        int port = Integer.parseInt(args[1]);
        Socket clientSocket = new Socket("localhost", port);
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            os = new DataOutputStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
            // Send a request in string format
            os.writeUTF("INIT");
            System.out.println("Client sent request: INIT");
            // read the response from the server
            String response = is.readUTF();
            System.out.println("Client received response: " + response);
            // display the required data here
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // When done, just close the connection and exit
            os.close();
            clientSocket.close();
        }
    }
}
