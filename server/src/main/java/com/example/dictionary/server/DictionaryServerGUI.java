package com.example.dictionary.server;

import java.io.*;
import java.sql.SQLException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.LogManager;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;

/**
 * ~ Dictionary Server GUI ~
 * This is a basic GUI for the Dictionary Server.
 * Integrated this with socket communication and
 * protocol implementation.
 *
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class DictionaryServerGUI extends JFrame {
    // Logger configuration
    private static Logger logger = LogManager.getLogger(DictionaryServerGUI.class);

    // GUI Components
    private JLabel connectionsCountLabel;
    private JTextArea logsArea;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusIndicator;
    private JLabel connectionsLabel;

    // Server logic
    int port;
    DictionaryServer dictionaryServer;

    public DictionaryServerGUI(int port, String initialDictionaryFile) throws SQLException, IOException {
        initializeGUI();

        initializeGuiAppender();

        this.port = port;

        // Initialize dictionary server
        dictionaryServer = new DictionaryServer(initialDictionaryFile);
        // Add connections label as an observer to count of active connections
        dictionaryServer.addConnectionListener(newCount -> {
            SwingUtilities.invokeLater(() -> {
                connectionsCountLabel.setText(Integer.toString(newCount));
            });
        });
    }

    /**
     * Create an custom appender to allow operational logs to be printed to the GUI
     * GUI must be initialized first
     */
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

    private void initializeGUI() {
        setTitle("Dictionary Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        add(createConnectionsPanel(), BorderLayout.NORTH);
        add(createLogsPanel(), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createConnectionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(new TitledBorder("Server Control"));

        // Start button
        startButton = new JButton("Start");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        // Stop button
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false); 
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer(); 
            }
        });

        // Status indicator (a colored circle using Unicode)
        statusIndicator = new JLabel("●");
        statusIndicator.setForeground(Color.RED);

        // Active connections count
        connectionsLabel = new JLabel("Active Connections: ");
        connectionsCountLabel = new JLabel("0");

        // Add components to panel in order
        panel.add(startButton);
        panel.add(stopButton);
        panel.add(statusIndicator);
        panel.add(connectionsLabel);
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

    public void startServer() {
        dictionaryServer.start(port);
        statusIndicator.setForeground(Color.GREEN);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    public void stopServer() {
        dictionaryServer.stop();
        statusIndicator.setForeground(Color.RED);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public static void main(String args[]) throws IOException, SQLException {
        // Parse command line arguments
        String initialDictionaryFile;
        int port;
        try {
            port = Integer.parseInt(args[0]);
            initialDictionaryFile = args[1];
        } catch (Throwable e) {
            logger.error("An error occurred while parsing command line arguments.", e);
            throw new RuntimeException(e);
        }

        // Initialize and display the GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Build GUI on EDT
                DictionaryServerGUI gui;

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // Will use default look and feel
                }

                try {
                    gui = new DictionaryServerGUI(port, initialDictionaryFile);
                    gui.setVisible(true);
                    logger.info("Dictionary server initialized!");
                    logger.debug("Initial list of words:\n" + gui.dictionaryServer.databaseConnector.getListOfWords());
                } catch (Throwable e) {
                    logger.error("An error occurred while initializing server", e);
                    throw new RuntimeException(e);
                }
            }
        });

    }
}
