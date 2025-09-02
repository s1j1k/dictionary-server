import java.io.*;
import java.net.*;

import com.google.gson.Gson;

        

// FIXME add my name here etc.

public class ClientConnection {
    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;
    Gson gson;

    // FIXME use print reader / buffered stuff etc.

    public ClientConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        os = new DataOutputStream(socket.getOutputStream());
        is = new DataInputStream(socket.getInputStream());

        // FIXME should I have just one instance or multiple ?
        gson = new Gson();
    }

    public void sendRequest(Request req) {
        // Send request to server in JSON format
        String json = gson.toJson(req);
        sendMessage(json);
    }

    public void sendMessage(String msg) throws IOException {
        // out.println(msg);
        // Send a request in string format
        os.writeUTF(msg);
        // System.out.println("Client sent request: ", msg);
    }

    public String receiveMessage() throws IOException {
        return is.readUTF();
    }
}
