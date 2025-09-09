package com.example.dictionary.client;

import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;

import com.example.dictionary.common.Request;
import com.example.dictionary.common.Response;

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
    }

    // TODO handle different types of errors here separately
    /**
     * Ping the server and raise an exception if it doesn't respond
     * 
     * @throws IOException
     * @throws UnknownHostException
     */
    public void pingServer() throws UnknownHostException, IOException {
        // Confirm connection is working fine
        Request request = new Request("ping");

        // Send the request in JSON format
        Response response = null;
        try {
            String jsonResponseString = sendRequest(request);
            response = gson.fromJson(jsonResponseString, Response.class);
            if (!"success".equals(response.getStatus())) {
                throw new IOException("Did not receive success status from server.");
            }
        } catch (IOException e) {
            // Throw an exception
            throw new IOException(e);
        } 
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

    String getHost() {return host;}

}
