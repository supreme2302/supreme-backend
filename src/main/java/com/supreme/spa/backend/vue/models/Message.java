package com.supreme.spa.backend.vue.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    private String message;
    @JsonCreator
    public Message(
            @JsonProperty("message") Enum message
    ) {
        this.message = message.toString();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
