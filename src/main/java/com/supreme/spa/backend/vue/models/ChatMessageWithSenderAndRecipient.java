package com.supreme.spa.backend.vue.models;

import lombok.Data;

@Data
public class ChatMessageWithSenderAndRecipient extends ChatMessage {
    private String senderEmail;
    private String senderUsername;
}
