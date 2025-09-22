package com.example.dictionary.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ~ Dictionary Server Class ~
 * Handles backend server socket communication.
 * 
 */
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

    public DictionaryServer(String intialDictionaryFile, int delayMillis) throws IOException, SQLException {
        // Initialize the dictionary database
        databaseConnector = new DatabaseConnector(intialDictionaryFile, delayMillis);
    }

    // Logic to pass number of active connections to the GUI
    // Observer pattern
    public interface ConnectionListener {
        void onConnectionCountChanged(int newCount);
    }

    private final List<ConnectionListener> listeners = new ArrayList<>();

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    private void notifyGui() {
        int count = numActiveConnections.get();
        for (ConnectionListener l : listeners) {
            l.onConnectionCountChanged(count);
        }
    }

    public void incrementNumActiveConnections() {
        numActiveConnections.incrementAndGet();
        notifyGui();
    }

    public void decrementNumActiveConnections() {
        numActiveConnections.decrementAndGet();
        notifyGui();
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
        logger.info("Starting server...");
        running = true;
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
        if (running) {
            serverThread.start();
        }
    }

    public void stop() {
        logger.info("Stopping server...");
        // NOTE: existing connections will remain connected. 
        // New connections will be refused.
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (threadPool != null) {
                threadPool.shutdown();
            }
        } catch (Exception ignored) {

        }

    }
}
