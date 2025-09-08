package com.example.dictionary.client;

import java.io.*;
import java.net.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;

import com.example.dictionary.common.Request;
        

// FIXME add my name here etc.
// NOTE this could just be a function in clientGUI but it allows for extension to a reused connection

public class ClientConnection {
    private String host;
    private int port;
    Gson gson = new Gson();

    // Logger
    private static Logger logger = LogManager.getLogger(ClientConnection.class);

    public ClientConnection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        // Confirm connection is working fine
        Socket socket = new Socket(host, port);
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        DataInputStream is = new DataInputStream(socket.getInputStream());
        socket.close();
        os.close();
        is.close();
    }

    public String sendRequest(Request req) throws IOException {
        // Create a new connection for each request 
        Socket socket = new Socket(host, port);
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        DataInputStream is = new DataInputStream(socket.getInputStream());

        // Send request to server in JSON format
        String json = gson.toJson(req);
        os.writeUTF(json);

        // Receive the server response
        String response = is.readUTF(); 
        logger.info("Client received response: " + response);

        // Close the socket each time
        socket.close();

        return response;
    }

  
}
