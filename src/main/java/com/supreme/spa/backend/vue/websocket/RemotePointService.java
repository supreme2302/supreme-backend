package com.supreme.spa.backend.vue.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supreme.spa.backend.vue.models.User;
import com.supreme.spa.backend.vue.services.UserService;
import com.supreme.spa.backend.vue.websocket.event.GetMessages;
import com.supreme.spa.backend.vue.websocket.event.GetMessagesRouteUpdate;
import com.supreme.spa.backend.vue.websocket.event.Message;
import com.supreme.spa.backend.vue.websocket.event.ResponseForList;
import com.supreme.spa.backend.vue.websocket.exception.SupremeWebSocketException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RemotePointService {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(RemotePointService.class);
    private final ObjectMapper objectMapper;

    private final UserService userService;

    @Autowired
    public RemotePointService(UserService userService,
                              ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public void registerUser(@NotNull String user, @NotNull WebSocketSession webSocketSession) {
        if (sessions.containsKey(user)) {
            return;
        }
        sessions.put(user, webSocketSession);
    }

    public boolean isConnected(@NotNull String user) {
        return sessions.containsKey(user) && sessions.get(user).isOpen();
    }

    public void removeUser(@NotNull String user) {
        sessions.remove(user);
    }

    public void cutDownConnection(@NotNull String user, @NotNull CloseStatus closeStatus) {
        final WebSocketSession webSocketSession = sessions.get(user);
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close(closeStatus);
            } catch (IOException ignore) {
            }
        }
    }

    public void sendMessageToUser(String recipientEmail, @NotNull Message message) throws SupremeWebSocketException {
        final WebSocketSession webSocketSession = sessions.get(recipientEmail);
        if (webSocketSession == null) {
            throw new SupremeWebSocketException("no session for user " + recipientEmail);
        }
        if (!webSocketSession.isOpen()) {
            throw new SupremeWebSocketException("session is closed or not exsists");
        }
        try {
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            throw new SupremeWebSocketException("Unnable to send message: " + e);
        }
    }

    public void sendMessagesToUser(int recipientId, List<? extends Message> messages, Class<? extends Message> clazz) throws SupremeWebSocketException {
        User user = userService.getUserById(recipientId);
        final WebSocketSession webSocketSession = sessions.get(user.getEmail());
        if (webSocketSession == null) {
            throw new SupremeWebSocketException("no session for user " + user.getEmail());
        }
        if (!webSocketSession.isOpen()) {
            throw new SupremeWebSocketException("session is closed or not exsists");
        }
        ResponseForList responseForList = new ResponseForList();
        responseForList.setMessages(messages);
        if (clazz.equals(GetMessages.class)) {
            responseForList.setClazz("GetMessages");
        } else if (clazz.equals(GetMessagesRouteUpdate.class)) {
            responseForList.setClazz("GetMessagesRouteUpdate");
        }
        try {
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseForList)));
        } catch (IOException e) {
            throw new SupremeWebSocketException("Unnable to send message: " + e);
        }

    }
}
