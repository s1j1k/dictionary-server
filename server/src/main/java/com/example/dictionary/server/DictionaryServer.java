package com.example.dictionary.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DictionaryServer {

    DatabaseConnector databaseConnector;
    ServerSocket serverSocket;
    ExecutorService threadPool;
    Thread serverThread;

    boolean running;

    private static Logger logger = LogManager.getLogger(ClientHandler.class);

    // Maximum number of threads in the thread pool
    static final int MAX_TH = 10;

    // Track the number of active connections
    // Use Atomic to make it thread safe
    private final AtomicInteger numActiveConnections = new AtomicInteger(0);

    public DictionaryServer(String intialDictionaryFile) throws IOException, SQLException {
        // Initialize the dictionary database
        databaseConnector = new DatabaseConnector(intialDictionaryFile);
    }

    public int incrementAndGetNumActiveConnections() {
        return numActiveConnections.incrementAndGet();
    }

    public int decremenAndGettNumActiveConnections() {
        return numActiveConnections.decrementAndGet();
    }

    // callback for GUI to listen to connection updates
    private Consumer<Integer> connectionListener;

    public void setConnectionListener(Consumer<Integer> listener) {
        this.connectionListener = listener;
    }

    private void notifyConnections(int count) {
        if (connectionListener != null) {
            connectionListener.accept(count);
        }
    }

    /**
     * Start the server on the given port
     * 
     * @param port
     */
    public void start(int port) {
        if (serverThread != null && serverThread.isAlive()) {
            return; // already running
        }
        running = true;
        logger.info("Starting server...");
        serverThread = new Thread(() -> {
            try {
                // Register service on the given port
                serverSocket = new ServerSocket(port);

                // Create a thread pool with MAX_TH number of threads
                threadPool = Executors.newFixedThreadPool(MAX_TH);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted a connection!");
                    threadPool.execute(new ClientHandler(clientSocket, this));
                    // FIXME increment number of actual connections in GUI
                }
            } catch (IOException e) {
                // Ignore socket errors when server is shut down
                if (running) {
                    // Log the error
                    logger.error("An error occurred while connecting to client.", e);
                }
            } finally {
                // Clean shutdown if loop exits
                stop();
            }
        });
        // Try to launch the thread again if we exited our loop
        serverThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
            if (threadPool != null) {
                threadPool.shutdown();
            }
        }
    }
}
