package com.supreme.spa.backend.vue.models;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ChatMessage {
    private int id;
    private int senderId;
    private int recipientId;
    private String content;
    private Timestamp date;
    private String recipientImage;
}
