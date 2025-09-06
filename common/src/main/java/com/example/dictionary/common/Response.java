package com.example.dictionary.common;

public class Response {
    private String status;
    private String result;

    // Constructors, getters, setters
    public Response() {}
    public Response(String status, String result) {
        this.status = status;
        this.result = result;
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}