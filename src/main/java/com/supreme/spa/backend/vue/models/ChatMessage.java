package com.supreme.spa.backend.vue.models;

import com.supreme.spa.backend.vue.websocket.event.Message;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class ChatMessage extends Message {
    private int id;
    private int senderId;
    private int recipientId;
    private String content;
    private Timestamp date;
    private String recipientImage;
}
