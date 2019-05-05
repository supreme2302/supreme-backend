package com.supreme.spa.backend.vue.websocket.event;

import lombok.Data;

@Data
public class GetMessagesRouteUpdate extends Message {
    int recipientId;
    int senderId;
}