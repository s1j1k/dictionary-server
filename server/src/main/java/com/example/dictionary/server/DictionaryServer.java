package com.example.dictionary.server;

import static java.util.concurrent.Executors.newFixedThreadPool;
import java.io.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

import com.example.dictionary.common.Request;
import com.example.dictionary.common.Response;
import com.google.gson.Gson;

// TODO server gui
//class DictionaryMonitor {}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DictionaryServer dictionaryServer;

    Gson gson = new Gson();

    public ClientHandler(Socket clientSocket, DictionaryServer dictionaryServer) {
        this.clientSocket = clientSocket;
        this.dictionaryServer = dictionaryServer;
    }

    public void run() throws RuntimeException {
        DataOutputStream dataOut = null;
        OutputStream streamOut = null;
        // String request = null;
        // String response = null;

        // Get a communication stream associated with the socket
        DataInputStream is = null;
        DataOutputStream os = null;
        try {
            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());
            String json = is.readUTF();
            System.out.println("Server received request: " + json);

            Request request = gson.fromJson(json, Request.class);
            Response response = null;

            // Handle search type request
            // FIXME should it be command or like type (?)
            switch (request.getCommand()) {
                case "search":
                    // TODO search in the DB and return the meanings
                    String result = dictionaryServer.databaseConnector.searchWord(request.getWord());
                    // FIXME define a fail ??
                    String status = "success";
                    // FIXME add javadoc
                    response = new Response(status, result);
                    break;

                case "remove": // remove word
                // TODO
                    break;

                case "add": // add meaning
                // TODO
                    break;

                case "update": // upadte meaning
                // TODO
                    break;

                default:
                    // FIXME raise an exception
                    // TODO return an error
                    // FIXME make this more detailed
                    System.out.println("Invalid request command received");
                    // FIXME make response status either success or fail
                    response = new Response("fail", "Invalid request command received");
                    // TODO default response to fail if try/catch to show an error occured
            }

            // TODO send back the response 
            if (response == null) {
                response = new Response("fail", "Failed to form response");
            }
            String jsonResponseString = gson.toJson(response);
            os.writeUTF(jsonResponseString);

            // FIXME handle as Request objects

            // handle the particular request here
            // NOTE you are not required to produce a list of words for scrolling
            // TODO add this optional feature at the end
            // if (request.equals("INIT")) {
            // // fixme is this reference of the OK with multi threading?
            // // todo make asynchronous (blocking) at the database connector side (?)
            // String wordList = dictionaryServer.databaseConnector.getListOfWords();
            // os.writeUTF(wordList);
            // System.out.println("Server sent words list: " + wordList);
            // }

            // TODO search word meaning

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
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

public class DictionaryServer {

    DatabaseConnector databaseConnector;

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
            System.out.println("Dictionary server initialized!");
            System.out.println("Initial list of words:\n" + dictionaryServer.databaseConnector.getListOfWords());

            // Register service on the given port
            serverSocket = new ServerSocket(port);

            // creating a thread pool with MAX_TH number of threads
            ExecutorService threadPool = newFixedThreadPool(MAX_TH);

            System.out.println("Waiting for client connections...");

            // accept multiple connections and use multiple threads
            while (true) {
                // create a thread and process any input clients requests?
                Socket clientSocket = serverSocket.accept(); // Wait and accept a connection
                System.out.println("Accepted a connection.");
                // create a thread to handle this client
                // FIXME should we just hand in the database connector?
                Runnable task = new ClientHandler(clientSocket, dictionaryServer);
                threadPool.execute(task);
                // todo make an option to close the application and exit this loop
            }

            // pool is shutdown
            // threadPool.shutdown();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
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
