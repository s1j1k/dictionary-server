package com.example.dictionary.client;

import java.io.*;
import java.net.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;

import com.example.dictionary.common.Request;
import com.example.dictionary.common.Response;

/**
 * Client Connection class
 * Provides a persistent socket connection
 *  to be used for all messages with the server
 *
 * @author Sally Arnold
 *         Student ID: 992316
 */

public class ClientConnection {
    private String host;
    private int port;
    private Socket socket; // FIXME do we need to allow closing / reopening the socket
    private DataOutputStream os;
    private DataInputStream is;

    Gson gson = new Gson();

    // Logger
    private static Logger logger = LogManager.getLogger(ClientConnection.class);

    public ClientConnection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    /**
     * Ping the server and raise an exception if it doesn't respond
     * 
     * @throws IOException - if the connection to server was unsuccessful
     */
    public void pingServer() throws IOException {
        socket = new Socket(host, port);
        os = new DataOutputStream(socket.getOutputStream());
        is = new DataInputStream(socket.getInputStream());

        // Send a ping request to server
        Request request = new Request("ping");
        Response response = null;
        try {
            String jsonResponseString = sendRequest(request);
            response = gson.fromJson(jsonResponseString, Response.class);
            if (!"success".equals(response.getStatus())) {
                throw new IOException("Did not receive success status from server.");
            }
        } catch (IOException e) {
            // Throw an exception to be handled in overarching continuous ping thread
            throw new IOException(e);
        } finally {
            // If failed, discard the socket and its streams
            close();
        }
    }

    public String sendRequest(Request req) throws IOException {
        // Send request to server in JSON format
        String json = gson.toJson(req);
        os.writeUTF(json);
        logger.info("Client sent request: " + json);

        // Receive the server response
        String response = is.readUTF();
        logger.info("Client received response: " + response);

        return response;
    }

    /**
     * Close the socket and streams to free resources for graceful exit
     */
    public void close() {
        try {
            if (is != null) {
                is.close();
            }

        } catch (IOException ignored) {
        }

        try {
            if (os != null) {
                os.close();
            }

        } catch (IOException ignored) {
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        } catch (IOException ignored) {
        }
    }

}
