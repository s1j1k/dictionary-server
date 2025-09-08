package com.example.dictionary.server;

import static java.util.concurrent.Executors.newFixedThreadPool;
import java.io.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.example.dictionary.common.Request;
import com.example.dictionary.common.Response;
import com.google.gson.Gson;

// TODO server gui
//class DictionaryMonitor {}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DictionaryServer dictionaryServer;

    Gson gson = new Gson();

    private static Logger logger = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Socket clientSocket, DictionaryServer dictionaryServer) {
        this.clientSocket = clientSocket;
        this.dictionaryServer = dictionaryServer;
    }

    public void run() throws RuntimeException {
        // Get a communication stream associated with the socket
        DataInputStream is = null;
        DataOutputStream os = null;
        try {
            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());
            String json = is.readUTF();
            logger.info("Server received request: " + json);

            Request request = gson.fromJson(json, Request.class);
            Response response = null;

            // TODO first confirm request is good or wrap in try/catch and a function

            switch (request.getCommand()) {
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
            throw new RuntimeException(e);
        } finally {
            // When done, just close the connection and exit
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Close the connection after each request
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

public class DictionaryServer {

    DatabaseConnector databaseConnector;

    private static Logger logger = LogManager.getLogger(DictionaryServer.class);

    // Maximum number of threads in the thread pool
    static final int MAX_TH = 10;

    public DictionaryServer(String intialDictionaryFile) throws SQLException, IOException {
        // load initial dictionary data from txt file using SQL statements
        // TODO do something with the file ?? or should that be already done?
        // FIXME can we reuse it across multiple threads?
        this.databaseConnector = new DatabaseConnector(intialDictionaryFile);
        // todo read back the data in the database
    }

    public static void main(String args[]) throws IOException, SQLException {

        ServerSocket serverSocket = null;
        try {
            // TODO proper error handling for missing arguments here
            int port = Integer.parseInt(args[0]);
            String initialDictionaryFile = args[1];

            // initialize dictionary server
            // FIXME does it even make sense to instantiate oneself during main fil
            // execution?
            DictionaryServer dictionaryServer = new DictionaryServer(initialDictionaryFile);

            // FIXME remove - for debugging
            // TODO convert to like a test
            // Print the word list
            // TODO proper logging
            logger.info("Dictionary server initialized!");
            logger.info("Initial list of words:\n" + dictionaryServer.databaseConnector.getListOfWords());

            // Register service on the given port
            serverSocket = new ServerSocket(port);

            // creating a thread pool with MAX_TH number of threads
            ExecutorService threadPool = newFixedThreadPool(MAX_TH);

            logger.info("Waiting for client connections...");

            // accept multiple connections and use multiple threads
            while (true) {
                // create a thread and process any input clients requests?
                Socket clientSocket = serverSocket.accept(); // Wait and accept a connection
                logger.info("Accepted a connection.");
                // create a thread to handle this client
                // FIXME should we just hand in the database connector?
                Runnable task = new ClientHandler(clientSocket, dictionaryServer);
                threadPool.execute(task);
                // todo make an option to close the application and exit this loop
            }

            // FIXME shut it down?
            // pool is shutdown
            // threadPool.shutdown();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // FIXME close client here also?
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
