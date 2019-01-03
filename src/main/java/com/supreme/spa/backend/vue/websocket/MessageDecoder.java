package com.supreme.spa.backend.vue.websocket;

import com.google.gson.Gson;
import com.supreme.spa.backend.vue.models.ChatMessage;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class MessageDecoder implements Decoder.Text<ChatMessage> {

    private static Gson gson = new Gson();

    @Override
    public ChatMessage decode(String s) throws DecodeException {
        ChatMessage message = gson.fromJson(s, ChatMessage.class);
        return message;
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}
