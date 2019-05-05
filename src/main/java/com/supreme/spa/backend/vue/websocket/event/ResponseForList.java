package com.supreme.spa.backend.vue.websocket.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class ResponseForList extends Message {
    @JsonProperty("class")
    private String clazz;
    private List<? extends Message> messages;

    public List<? extends Message> getMessages() {
        return messages;
    }

    public void setMessages(List<? extends Message> messages) {
        this.messages = messages;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
