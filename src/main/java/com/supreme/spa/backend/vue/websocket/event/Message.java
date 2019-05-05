package com.supreme.spa.backend.vue.websocket.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.supreme.spa.backend.vue.models.ChatMessage;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GetMessages.class, name = "GetMessages"),
        @JsonSubTypes.Type(value = GetMessagesRouteUpdate.class, name = "GetMessagesRouteUpdate"),
        @JsonSubTypes.Type(value = ChatMessage.class, name = "ChatMessage")
})
public abstract class Message {}
