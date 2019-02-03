package com.supreme.spa.backend.vue.models;

import java.sql.Timestamp;

public class ChatMessage {
    private int id;
    private String sender;
    private String recipient;
    private String content;
    private Timestamp date;

    public ChatMessage() {
    }

    ;

    public ChatMessage(String sender, String recipient, String content, Timestamp date, int id) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.date = date;
        this.id = id;
    }

    @Override
    public String toString() {
        return super.toString();
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
