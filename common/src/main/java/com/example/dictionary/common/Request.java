package com.example.dictionary.common;

public class Request {
    private String command;
    private String word;
    private String meanings;

    // Constructors, getters, setters
    public Request() {
    }

    public Request(String command, String word) {
        this.command = command;
        this.word = word;
    }

    public Request(String command, String word, String meanings) {
        this(command, word);
        this.meanings = meanings;
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
}
