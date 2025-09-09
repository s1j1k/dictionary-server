package com.example.dictionary.server;

import static java.util.concurrent.Executors.newFixedThreadPool;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.LogManager;
import com.example.dictionary.common.Request;
import com.example.dictionary.common.Response;
import com.google.gson.Gson;

import javax.management.RuntimeErrorException;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;

// TODO server gui
//class DictionaryMonitor {}
// TODO rename to server gui

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DictionaryServerGUI dictionaryServer;

    private static Logger logger = LogManager.getLogger(ClientHandler.class);

    Gson gson = new Gson();

    public ClientHandler(Socket clientSocket, DictionaryServerGUI dictionaryServer) {
        this.clientSocket = clientSocket;
        this.dictionaryServer = dictionaryServer;
    }

    public void run() throws RuntimeException {
        // Get a communication stream associated with the socket
        DataInputStream is = null;
        DataOutputStream os = null;
        try {
            dictionaryServer.incrementNumActiveConnections();

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
                dictionaryServer.decrementNumActiveConnections();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}

public class DictionaryServerGUI extends JFrame {

    DatabaseConnector databaseConnector;

    // Logger configuration
    private static Logger logger = LogManager.getLogger(DictionaryServerGUI.class);

    // Maximum number of threads in the thread pool
    static final int MAX_TH = 10;

    // GUI Components
    private JLabel connectionsCountLabel;
    private JTextArea logsArea;

    // Track the number of active connections
    // Use Atomic to make it thread safe
    private final AtomicInteger numActiveConnections = new AtomicInteger(0);

    public DictionaryServerGUI(String intialDictionaryFile) throws SQLException, IOException {
        // Initialize the dictionary database
        databaseConnector = new DatabaseConnector(intialDictionaryFile);

        initializeGUI();

        // Start the GUI appender for Operational logs panel (GUI must be initialized
        // first)
        // GUI appender logger configuration
        initializeGuiAppender();
    }

    private void initializeGuiAppender() {
        Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                .withPattern("[%d{HH:mm:ss}] %-5level: %msg%n")
                .build();
        Filter filter = ThresholdFilter.createFilter(Level.INFO, Filter.Result.ACCEPT, Filter.Result.DENY);

        // Build the appender
        GuiAppender guiAppender = new GuiAppender("GuiAppender", filter, layout, true, logsArea);

        guiAppender.start();

        // Attach it to the root logger so it receives the same events
        org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) LogManager
                .getRootLogger();
        coreLogger.addAppender(guiAppender);
    }

    public JTextArea getLogsArea() {
        return this.logsArea;
    }

    // TODO show the number of active connections, operational logs, server
    // start/stop
    private void initializeGUI() {
        setTitle("Dictionary Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        add(createActiveConnectionsPanel(), BorderLayout.NORTH);
        add(createLogsPanel(), BorderLayout.CENTER);
        // add(createStartStopPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createActiveConnectionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Active Connections"));

        connectionsCountLabel = new JLabel("0");
        // statusLabel.setForeground(Color.RED);
        panel.add(connectionsCountLabel);

        return panel;
    }

    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Operational Logs"));

        logsArea = new JTextArea(8, 50);
        logsArea.setEditable(false);
        logsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logsArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Use synchronized to make it thread safe
    public synchronized void incrementNumActiveConnections() {
        // Increase number of active connections and display it
        connectionsCountLabel.setText(Integer.toString(numActiveConnections.incrementAndGet()));
    }

    // Use synchronized to make it thread safe
    public synchronized void decrementNumActiveConnections() {
        // Decrease number of active connections and display it
        connectionsCountLabel.setText(Integer.toString(numActiveConnections.decrementAndGet()));
    }

    public static void main(String args[]) throws IOException, SQLException {
        // Parse command line arguments
        int port;
        String initialDictionaryFile;
        try {
            port = Integer.parseInt(args[0]);
            initialDictionaryFile = args[1];
        } catch (Throwable e) {
            logger.error("An error occurred while parsing command line arguments.", e);
            throw new RuntimeException(e);
        }

        // Initialize and display the GUI
        // Initialize GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Build GUI on EDT
                DictionaryServerGUI dictionaryServer;
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // Will use default look and feel
                }

                try {

                    // initialize dictionary server gui

                    dictionaryServer = new DictionaryServerGUI(initialDictionaryFile);
                    dictionaryServer.setVisible(true);

                    logger.info("Dictionary server initialized!");
                    logger.info("Initial list of words:\n" + dictionaryServer.databaseConnector.getListOfWords());
                } catch (Throwable e) {
                    logger.error("An error occurred while initializing server", e);
                    throw new RuntimeException(e);
                }

                // Launch server socket in another thread so it doesn't block GUI
                Thread serverThread = new Thread(() -> {

                    ServerSocket serverSocket = null;
                    ExecutorService threadPool = null;

                    try {

                        // Register service on the given port
                        serverSocket = new ServerSocket(port);

                        // Create a thread pool with MAX_TH number of threads
                        threadPool = newFixedThreadPool(MAX_TH);

                        logger.info("Waiting for client connections...");

                        // accept multiple connections and use multiple threads
                        while (true) {
                            // create a thread and process any input clients requests?
                            Socket clientSocket = serverSocket.accept(); // Wait and accept a connection
                            logger.info("Accepted a connection.");
                            // NOTE do not increment number of active connections yet
                            // TODO implement
                            // Increase number of active connections
                            // FIXME do this from within the client handler
                            // dictionaryServer.incrementNumActiveConnections();
                            // create a thread to handle this client
                            // FIXME should we just hand in the database connector?
                            Runnable task = new ClientHandler(clientSocket, dictionaryServer);
                            threadPool.execute(task);
                            // TODO make an option to close the application and exit this loop
                            // TODO allow to close/open server
                        }

                    } catch (

                    IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            if (threadPool != null) {
                                threadPool.shutdown();
                            }
                        } catch (Throwable e) {
                            // Do nothing
                            logger.error("An error occurred while closing thread pool.", e);
                        }
                        try {
                            if (serverSocket != null) {
                                serverSocket.close();
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                serverThread.start(); // Launches in background

            }
        });

    }
}
