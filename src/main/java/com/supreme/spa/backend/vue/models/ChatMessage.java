package com.supreme.spa.backend.vue.models;

public class ChatMessage {
    private Integer from;
    private Integer to;
    private String content;

    @Override
    public String toString() {
        return super.toString();
    }


    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
