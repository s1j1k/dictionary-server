package com.example.dictionary.common;

/**
 * ~ Common Request Class ~
 * Provides the structure for client requests.
 */
public class Request {
    private String command;
    private String word;
    private String meanings;
    private String newMeaning;
    private String oldMeaning;

    // Constructors, getters, setters
    public Request() {
    }

    // Ping request type
    public Request(String command) {
        this.command = command;
    }

    public Request(String command, String word) {
        this.command = command;
        this.word = word;
    }

    public Request(String command, String word, String meanings) {
        this(command, word);

        if ("addMeaning".equals(command)) {
            this.newMeaning = meanings;
        }

        if ("addWord".equals(command)) {
            this.meanings = meanings;
        }
    }

    public Request(String command, String word, String oldMeaning, String newMeaning) {
        this(command, word);

        this.oldMeaning = oldMeaning;
        this.newMeaning = newMeaning;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeanings() {
        return meanings;
    }

    public void setMeanings(String meanings) {
        this.meanings = meanings;
    }

    public String getNewMeaning() {
        return newMeaning;
    }

    public void setNewMeaning(String newMeaning) {
        this.newMeaning = newMeaning;
    }

    public String getOldMeaning() {
        return oldMeaning;
    }

    public void setOldMeaning(String oldMeaning) {
        this.oldMeaning = oldMeaning;
    }
}
