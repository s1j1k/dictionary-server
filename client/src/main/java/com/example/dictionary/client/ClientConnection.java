package com.example.dictionary.client;

import java.io.*;
import java.net.*;

import com.google.gson.Gson;

import com.example.dictionary.common.Request;
        

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

    // FIXME should the response be a class in itself
    // TODO return as Response type (?)
    public String sendRequest(Request req) throws IOException {
        // Send request to server in JSON format
        String json = gson.toJson(req);
        sendMessage(json);

        // FIXME how to we receive a response here, are we supposed to wait or?
        String response = receiveMessage(); // FIXME should just use is/os directly here?

        // FIXME update the logging system
        System.out.println("Client received response: " + response);

        // TODO return as a Response object constructed from JSON not a string
        return response;
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
