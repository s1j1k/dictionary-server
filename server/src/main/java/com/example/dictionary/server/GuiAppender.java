package com.example.dictionary.server;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.Serializable;


/**
 * ~ Custom GUI Appender Class ~
 * This is a custom GUI appender that allows the 
 * server log4j2 logger to print to the GUI logs area.
 *
 */
public class GuiAppender extends AbstractAppender {

    private JTextArea logDisplayArea; // Reference to your JTextArea

    protected GuiAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, JTextArea logDisplayArea) {
        super(name, filter, layout, ignoreExceptions);
        this.logDisplayArea = logDisplayArea;
    }

    @Override
    public void append(LogEvent event) {
        if (event == null || logDisplayArea == null) {
            return;
        }

        // Format the log event
        final String message = getLayout().toSerializable(event).toString();

        // Update the JTextArea on the EDT
        SwingUtilities.invokeLater(() -> {
            logDisplayArea.append(message);
            logDisplayArea.setCaretPosition(logDisplayArea.getDocument().getLength()); // Scroll to bottom
        });
    }
}
