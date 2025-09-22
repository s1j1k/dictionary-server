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

/**
 * ~ Client Handler Class ~
 * Handles each connection as a thread
 *
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DictionaryServer dictionaryServer;

    private static Logger logger = LogManager.getLogger(ClientHandler.class);

    Gson gson = new Gson();

    public ClientHandler(Socket clientSocket, DictionaryServer dictionaryServer) {
        this.clientSocket = clientSocket;
        this.dictionaryServer = dictionaryServer;
    }

    public void run() {
        try (DataInputStream is = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream())) {

            dictionaryServer.incrementNumActiveConnections();

            while (true) {
                try {
                    String json = is.readUTF();
                    logger.info("Server received request: " + json);

                    Request request = gson.fromJson(json, Request.class);
                    Response response = handleRequest(request);

                    String jsonResponseString = gson.toJson(response);
                    os.writeUTF(jsonResponseString);
                    os.flush();

                } catch (IOException e) {
                    // Exit the loop when client disconnects
                    logger.info("Client disconnected or error occurred, closing handler.");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Error setting up client streams.", e);
        } finally {
            dictionaryServer.decrementNumActiveConnections();
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error closing client socket.", e);
            }
        }
    }

    private Response handleRequest(Request request) {
        try {
            switch (request.getCommand()) {
                case "ping":
                    return new Response("success", "Successfully pinged server.");
                case "searchWord":
                    return dictionaryServer.databaseConnector.searchWord(request.getWord());
                case "addWord":
                    return dictionaryServer.databaseConnector.addWord(request.getWord(), request.getMeanings());
                case "removeWord":
                    return dictionaryServer.databaseConnector.removeWord(request.getWord());
                case "addMeaning":
                    return dictionaryServer.databaseConnector.addMeaning(request.getWord(), request.getNewMeaning());
                case "updateMeaning":
                    return dictionaryServer.databaseConnector.updateMeaning(
                            request.getWord(), request.getOldMeaning(), request.getNewMeaning());
                default:
                    logger.info("Invalid request command received");
                    return new Response("fail", "Invalid request command received");
            }
        } catch (Exception e) {
            // Use the error message from database functions
            return new Response("fail", "Error: " + e.getMessage());
        }

    }

}
