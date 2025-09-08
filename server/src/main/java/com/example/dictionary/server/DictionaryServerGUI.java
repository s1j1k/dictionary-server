package com.example.dictionary.server;

import static java.util.concurrent.Executors.newFixedThreadPool;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.LogManager;
import com.example.dictionary.common.Request;
import com.example.dictionary.common.Response;
import com.google.gson.Gson;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
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
            // TODO implement that
            this.dictionaryServer.decrementNumActiveConnections();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    private int numActiveConnections;

    // GUI appender logger configuration
    Layout<? extends Serializable> layout = PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss}] %-5level: %msg%n")
            .build();
    Filter filter = ThresholdFilter.createFilter(Level.INFO, Filter.Result.ACCEPT, Filter.Result.DENY);

    // Build and start the appender
    GuiAppender guiAppender = new GuiAppender("GuiAppender", filter, layout, true, logsArea);

    public DictionaryServerGUI(String intialDictionaryFile) throws SQLException, IOException {
        this.databaseConnector = new DatabaseConnector(intialDictionaryFile);
        this.numActiveConnections = 0;

        // Start the GUI appender
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
        add(createStartStopPanel(), BorderLayout.SOUTH);

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

    // private JPanel createLogsPanel() {
    // JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    // panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // // Add loga panel
    // panel.add(());

    // return panel;
    // }

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

    void incrementNumActiveConnections() {
        // Increase number of active connections and display it
        this.numActiveConnections++;
        connectionsCountLabel.setText(Integer.toString(this.numActiveConnections));
    }

    void decrementNumActiveConnections() {
        // Decrease number of active connections and display it
        this.numActiveConnections--;
        connectionsCountLabel.setText(Integer.toString(this.numActiveConnections));
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
            DictionaryServerGUI dictionaryServer = new DictionaryServerGUI(initialDictionaryFile);

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
                // TODO implement
                // Increase number of active connections
                dictionaryServer.incrementNumActiveConnections();
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
