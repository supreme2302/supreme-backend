package com.supreme.spa.backend.vue.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class CommonWebSocketHandler extends TextWebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        LOGGER.info("CommonWebSocketHandler afterConnectionEstablished");
    }
}
