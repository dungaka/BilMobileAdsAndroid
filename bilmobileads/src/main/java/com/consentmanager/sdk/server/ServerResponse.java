package com.consentmanager.sdk.server;

public class ServerResponse {

    private int status;
    private String message;
    private int regulation;
    private String url;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRegulation(int regulation) {
        this.regulation = regulation;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    public int getRegulation(){
        return regulation;
    }
}
