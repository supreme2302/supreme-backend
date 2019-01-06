package com.supreme.spa.backend.vue.websocket;

import com.google.gson.Gson;
import com.supreme.spa.backend.vue.models.ChatMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RemotePointService {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void registerUser(@NotNull String user, @NotNull WebSocketSession webSocketSession) {
        if (sessions.containsKey(user)) {
            try {
                ChatMessage message = new ChatMessage();
                message.setContent("Hello test");
                webSocketSession.sendMessage(new TextMessage(gson.toJson(message)));
//                webSocketSession.close(new CloseStatus(403));
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void sendMessageToUser(@NotNull String user, @NotNull ChatMessage message) throws IOException {
        final WebSocketSession webSocketSession = sessions.get(user);
        if (webSocketSession == null) {
            throw new IOException("no session for user " + user);
        }
        if (!webSocketSession.isOpen()) {
            throw new IOException("session is closed or not exsists");
        }
        try {
            webSocketSession.sendMessage(new TextMessage(gson.toJson(message)));
        } catch (IOException e) {
            throw new IOException("Unnable to send message", e);
        }
    }
}
