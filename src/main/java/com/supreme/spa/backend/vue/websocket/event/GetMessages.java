package com.supreme.spa.backend.vue.websocket.event;

import lombok.Data;

@Data
public class GetMessages extends Message {
    int recipientId;
    int senderId;
}
