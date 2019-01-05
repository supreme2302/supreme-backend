package com.supreme.spa.backend.vue.models;

public class ChatMessage {
    private String from;
    private String to;
    private String content;

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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
