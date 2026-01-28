package com.example.ecostayapp.models;

import java.io.Serializable;

public class Notification implements Serializable {
    private String title;
    private String message;
    private String timestamp;
    private boolean isRead;

    public Notification() {}

    public Notification(String title, String message, String timestamp, boolean isRead) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}







