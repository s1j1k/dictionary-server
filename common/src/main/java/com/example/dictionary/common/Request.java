package com.example.dictionary.common;

import java.util.List;

public class Request {
    private String command;
    private String word;
    private List<String> meanings; // optional, used for ADD

    // Constructors, getters, setters
    public Request() {}
    public Request(String command, String word) {
        this.command = command;
        this.word = word;
    }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    // public List<String> getMeanings() { return meanings; }
    // public void setMeanings(List<String> meanings) { this.meanings = meanings; }
}
