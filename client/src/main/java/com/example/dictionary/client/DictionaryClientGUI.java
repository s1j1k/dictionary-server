package com.example.dictionary.client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.example.dictionary.common.Request;
import com.example.dictionary.common.Response;
import com.google.gson.Gson;

/**
 * ~ Dictionary Client GUI From Provided Skeleton ~
 * This is a basic GUI for the Dictionary Client.
 * Integrated this with socket communication and
 * protocol implementation.
 *
 */
public class DictionaryClientGUI extends JFrame {

    // GUI Components
    private JTextField wordField;
    private JTextArea meaningArea;
    private JTextField existingMeaningField;
    private JTextField newMeaningField;
    private JTextArea resultArea;
    private JButton searchButton;
    private JButton addWordButton;
    private JButton removeWordButton;
    private JButton addMeaningButton;
    private JButton updateMeaningButton;
    private JLabel statusLabel;

    // Gson for converting between objects and JSON strings
    Gson gson = new Gson();

    // Connection status - initially disconnected
    private boolean isConnected = false;

    // Connection to server
    private ClientConnection clientConnection;

    // Setter for client connection
    public void setClientConnection(ClientConnection connection) {
        this.clientConnection = connection;
    }

    // Logger
    private static Logger logger = LogManager.getLogger(DictionaryClientGUI.class);

    public DictionaryClientGUI() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Dictionary Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        add(createConnectionPanel(), BorderLayout.NORTH);
        add(createOperationsPanel(), BorderLayout.CENTER);
        add(createResultPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Connection Status"));

        statusLabel = new JLabel("Not Connected");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel);

        return panel;
    }

    private JPanel createOperationsPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search Word Panel
        mainPanel.add(createSearchPanel());

        // Add Word Panel
        mainPanel.add(createAddWordPanel());

        // Remove Word Panel
        mainPanel.add(createRemoveWordPanel());

        // Update Operations Panel
        mainPanel.add(createUpdatePanel());

        return mainPanel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Search Word"));

        wordField = new JTextField();
        searchButton = new JButton("Search");

        panel.add(new JLabel("Word:"), BorderLayout.WEST);
        panel.add(wordField, BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.EAST);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchWord();
            }
        });

        return panel;
    }

    private JPanel createAddWordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Add New Word"));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField addWordField = new JTextField();
        meaningArea = new JTextArea(3, 20);
        meaningArea.setLineWrap(true);
        meaningArea.setWrapStyleWord(true);
        JScrollPane meaningScroll = new JScrollPane(meaningArea);

        addWordButton = new JButton("Add Word");

        inputPanel.add(new JLabel("Word:"));
        inputPanel.add(addWordField);
        inputPanel.add(new JLabel("Meaning(s):"));
        inputPanel.add(meaningScroll);
        inputPanel.add(new JLabel(""));
        inputPanel.add(addWordButton);

        panel.add(inputPanel, BorderLayout.CENTER);

        addWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addWord(addWordField.getText(), meaningArea.getText());
            }
        });

        return panel;
    }

    private JPanel createRemoveWordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Remove Word"));

        JTextField removeWordField = new JTextField();
        removeWordButton = new JButton("Remove");

        panel.add(new JLabel("Word:"), BorderLayout.WEST);
        panel.add(removeWordField, BorderLayout.CENTER);
        panel.add(removeWordButton, BorderLayout.EAST);

        removeWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeWord(removeWordField.getText());
            }
        });

        return panel;
    }

    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Update Operations"));

        JPanel operationsPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField updateWordField = new JTextField();
        existingMeaningField = new JTextField();
        newMeaningField = new JTextField();

        addMeaningButton = new JButton("Add Meaning");
        updateMeaningButton = new JButton("Update Meaning");

        operationsPanel.add(new JLabel("Word:"));
        operationsPanel.add(updateWordField);
        operationsPanel.add(new JLabel("Existing Meaning:"));
        operationsPanel.add(existingMeaningField);
        operationsPanel.add(new JLabel("New Meaning:"));
        operationsPanel.add(newMeaningField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addMeaningButton);
        buttonPanel.add(updateMeaningButton);

        panel.add(operationsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        addMeaningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMeaning(updateWordField.getText(), newMeaningField.getText());
            }
        });

        updateMeaningButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMeaning(updateWordField.getText(),
                        existingMeaningField.getText(),
                        newMeaningField.getText());
            }
        });

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Results"));

        resultArea = new JTextArea(8, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Search for a word in the dictionary
     */
    private void searchWord() {
        String word = wordField.getText().trim();
        if (word.isEmpty()) {
            displayResult("Error: Please enter a word to search.");
            return;
        }

        // Form the request to server
        Request request = new Request("searchWord", word);

        // Send the request in JSON format
        Response response = null;
        try {
            String jsonResponseString = this.clientConnection.sendRequest(request);
            response = gson.fromJson(jsonResponseString, Response.class);
        } catch (IOException e) {
            // Log detailed error info
            logger.error("An error occured while connecting to server.", e);
            // Assume connection has been lost
            response = new Response("fail", "An error occured while connecting to server.");
            // Update the status to disconnected
            setConnectionStatus(false);
        }

        displayResult(response.getResult());

    }

    /**
     * Add a new word with meanings to the dictionary
     */
    private void addWord(String word, String meanings) {
        if (word.trim().isEmpty() || meanings.trim().isEmpty()) {
            displayResult("Error: Both word and meaning(s) are required.");
            return;
        }

        // Form the request to server
        // Assume different meanings are separated by newline
        Request request = new Request("addWord", word, meanings);

        // Send the request in JSON format
        Response response = null;
        try {
            String jsonResponseString = this.clientConnection.sendRequest(request);
            response = gson.fromJson(jsonResponseString, Response.class);
        } catch (IOException e) {
            // Log detailed error info
            logger.error("An error occured while connecting to server.", e);
            // Assume connection has been lost
            response = new Response("fail", "An error occured while connecting to server.");
            // Update the status to disconnected
            setConnectionStatus(false);
        }

        displayResult(response.getResult());
    }

    /**
     * Remove a word from the dictionary
     * You need to implement this
     */
    private void removeWord(String word) {
        if (word.trim().isEmpty()) {
            displayResult("Error: Please enter a word to remove.");
            return;
        }

        // Form the request to server
        // Assume different meanings are separated by newline OR comma
        Request request = new Request("removeWord", word);

        // Send the request in JSON format
        Response response = null;
        try {
            String jsonResponseString = this.clientConnection.sendRequest(request);
            response = gson.fromJson(jsonResponseString, Response.class);
        } catch (IOException e) {
            // Log detailed error info
            logger.error("An error occured while connecting to server.", e);
            // Assume connection has been lost
            response = new Response("fail", "An error occured while connecting to server.");
            // Update the status to disconnected
            setConnectionStatus(false);
        }

        displayResult(response.getResult());
    }

    /**
     * Add a new meaning to an existing word
     * You need to implement this
     */
    private void addMeaning(String word, String newMeaning) {
        if (word.trim().isEmpty() || newMeaning.trim().isEmpty()) {
            displayResult("Error: Both word and new meaning are required.");
            return;
        }

        // Form the request to server
        // Assume different meanings are separated by newline OR comma
        Request request = new Request("addMeaning", word, newMeaning);

        // Send the request in JSON format
        Response response = null;
        try {
            String jsonResponseString = this.clientConnection.sendRequest(request);
            response = gson.fromJson(jsonResponseString, Response.class);
        } catch (IOException e) {
            logger.error("An error occured while connecting to server.", e);
            // Assume connection has been lost
            response = new Response("fail", "An error occured while connecting to server.");
            // Update the status to disconnected
            setConnectionStatus(false);
        }

        displayResult(response.getResult());
    }

    /**
     * Update an existing meaning of a word
     * You need to implement this
     */
    private void updateMeaning(String word, String existingMeaning, String newMeaning) {
        if (word.trim().isEmpty() || existingMeaning.trim().isEmpty() || newMeaning.trim().isEmpty()) {
            displayResult("Error: Word, existing meaning, and new meaning are all required.");
            return;
        }

        // Form the request to server
        // Assume different meanings are separated by newline OR comma
        Request request = new Request("updateMeaning", word, existingMeaning, newMeaning);

        // Send the request in JSON format
        Response response = null;
        try {
            String jsonResponseString = this.clientConnection.sendRequest(request);
            response = gson.fromJson(jsonResponseString, Response.class);
        } catch (IOException e) {
            // Log detailed error info
            logger.error("An error occured while connecting to server.", e);
            // Assume connection has been lost
            response = new Response("fail", "An error occured while connecting to server.");
            // Update the status to disconnected
            setConnectionStatus(false);
        }

        displayResult(response.getResult());
    }

    /**
     * Display result in the result area
     */
    private void displayResult(String result) {
        resultArea.append(java.time.LocalTime.now() + ": " + result + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    /**
     * Update connection status
     * You should call this method when connection status changes
     */
    public void setConnectionStatus(boolean connected) {
        this.isConnected = connected;
        if (connected) {
            statusLabel.setText("Connected");
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel.setText("Not Connected");
            statusLabel.setForeground(Color.RED);
        }

        // Enable/disable buttons based on connection status
        searchButton.setEnabled(connected);
        addWordButton.setEnabled(connected);
        removeWordButton.setEnabled(connected);
        addMeaningButton.setEnabled(connected);
        updateMeaningButton.setEnabled(connected);
    }

    /**
     * 
     * Poll the server for connection with ping every 5 sections
     * 
     * Connection is recovered when a successful ping happens
     * This is the only way to go from disconnected to connected status
     */
    private void pingForConnection() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final ScheduledFuture<?>[] pingTaskHolder = new ScheduledFuture<?>[1];

        pingTaskHolder[0] = scheduler.scheduleAtFixedRate(() -> {
            try {
                clientConnection.pingServer();
                // if ping succeeds, client is connected
                logger.info("Ping successful. Client is connected!");
                setConnectionStatus(true);

            } catch (IOException e) {
                // ping failed, mark as disconnected, keep trying
                setConnectionStatus(false);
                logger.debug("Error occured while pinging server", e);
                logger.info("Ping failed, retrying...");
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Main method for testing GUI
     * You should modify this to include command line argument parsing
     */
    public static void main(String[] args) {
        // Parse command line arguments
        // Expected: java DictionaryClient.jar <server-address> <server-port>
        // <sleep-duration>
        String serverAddress;
        int port;

        try {
            serverAddress = new String(args[0]);
            port = Integer.parseInt(args[1]);
        } catch (Throwable e) {
            logger.error("An error occurred while parsing command line arguments.", e);
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // Will use default look and feel
                }

                DictionaryClientGUI gui = new DictionaryClientGUI();
                gui.setVisible(true);

                // Initialize socket connection
                try {
                    // Initialize persistent client connection
                    ClientConnection clientConnection = new ClientConnection(serverAddress, port);
                    gui.setClientConnection(clientConnection);
                    // Trigger the client to continually ping server for connection status
                    // Allows client to persist in case server is down when started
                    gui.pingForConnection();
                } catch (IOException e) {
                    logger.error("Failed to initialize client.", e);
                    // Graceful exit to free resources
                    gui.clientConnection.close();
                }

            }
        });
    }
}