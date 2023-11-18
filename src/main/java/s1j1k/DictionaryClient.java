package s1j1k;

import java.io.*;
import java.net.*;

public class DictionaryClient {
    private DictGUI gui;
    DictionaryClient() {
        this.gui = new DictGUI(500,500);
    }

    public static void main(String args[]) throws IOException {
        DictionaryClient dictionaryClient = new DictionaryClient();
        String serverAddress = new String(args[0]);
        int port = Integer.parseInt(args[1]);
        Socket clientSocket = new Socket(serverAddress, port);
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            os = new DataOutputStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
            // Send a request in string format
            os.writeUTF("INIT");
            System.out.println("Client sent request: INIT");
            // read the response from the server
            // todo need to receive a list of words from Server
            // note how do you know that an input stream will be available??
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
