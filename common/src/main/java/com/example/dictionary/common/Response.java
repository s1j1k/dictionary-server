package com.example.dictionary.common;

/**
 * 
 * Response class
 */
public class Response {
    private String status;
    private String result;

    // Constructors, getters, setters
    /**
     * 
     * Response class constructor
     * @param status "success" or "fail"
     * @param result string containing the content of the response to the client
     */
    public Response(String status, String result) {
        this.status = status;
        this.result = result;
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}