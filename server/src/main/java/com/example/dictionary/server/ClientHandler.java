package com.example.dictionary.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.dictionary.common.Request;
import com.example.dictionary.common.Response;
import com.google.gson.Gson;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DictionaryServer dictionaryServer;

    private static Logger logger = LogManager.getLogger(ClientHandler.class);

    Gson gson = new Gson();

    public ClientHandler(Socket clientSocket, DictionaryServer dictionaryServer) {
        this.clientSocket = clientSocket;
        this.dictionaryServer = dictionaryServer;
    }

    public void run() throws RuntimeException {
        // Get a communication stream associated with the socket
        DataInputStream is = null;
        DataOutputStream os = null;
        try {
            // FIXME implement some other logic for counting number of connections
            // dictionaryServer.incrementNumActiveConnections();

            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());
            String json = is.readUTF();
            logger.info("Server received request: " + json);

            Request request = gson.fromJson(json, Request.class);
            Response response = null;

            switch (request.getCommand()) {
                case "ping":
                    try {
                        response = new Response("success", "Successfully pinged server.");
                    } catch (Exception e) {
                        response = new Response("fail", "Error: " + e.getMessage());
                    }
                    break;

                case "searchWord":
                    try {
                        response = dictionaryServer.databaseConnector.searchWord(request.getWord());
                    } catch (Exception e) {
                        response = new Response("fail", "Error: " + e.getMessage());
                    }
                    break;

                case "addWord":
                    try {
                        response = dictionaryServer.databaseConnector.addWord(request.getWord(), request.getMeanings());
                    } catch (Exception e) {
                        response = new Response("fail", "Error: " + e.getMessage());
                    }
                    break;

                case "removeWord":
                    try {
                        response = dictionaryServer.databaseConnector.removeWord(request.getWord());
                    } catch (Exception e) {
                        response = new Response("fail", "Error: " + e.getMessage());
                    }
                    break;

                case "addMeaning":
                    try {
                        response = dictionaryServer.databaseConnector.addMeaning(request.getWord(),
                                request.getNewMeaning());
                    } catch (Exception e) {
                        response = new Response("fail", "Error: " + e.getMessage());
                    }
                    break;

                case "updateMeaning":
                    try {
                        response = dictionaryServer.databaseConnector.updateMeaning(request.getWord(),
                                request.getOldMeaning(), request.getNewMeaning());
                    } catch (Exception e) {
                        response = new Response("fail", "Error: " + e.getMessage());
                    }
                    break;

                default:
                    logger.info("Invalid request command received");
                    response = new Response("fail", "Invalid request command received");
            }

            // Send back the response
            if (response == null) {
                response = new Response("fail", "Failed to form response");
            }
            String jsonResponseString = gson.toJson(response);
            os.writeUTF(jsonResponseString);

        } catch (IOException e) {
            // Don't close the server because of one client's IOException
            logger.error("An error occurred while handling client.", e);
        } finally {
            // When done, just close the connection and exit
            try {
                is.close();
            } catch (IOException e) {
                logger.error("An error occurred while closing input stream.", e);
            }
            try {
                os.close();
            } catch (IOException e) {
                logger.error("An error occurred while closing output stream.", e);
            }
            // Close the connection after each request
            try {
                clientSocket.close();
                // TODO use some kind of hook to make this update in the GUI
                // dictionaryServer.decrementNumActiveConnections();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
