package com.supreme.spa.backend.vue.websocket;

import com.google.gson.Gson;
import com.supreme.spa.backend.vue.models.ChatMessage;
import com.supreme.spa.backend.vue.services.UserService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final CloseStatus ACCESS_DENIED = new CloseStatus(4500, "Not logged in. Access denied");
    private final @NotNull UserService userService;
    private final @NotNull RemotePointService remotePointService;
    private final @NotNull Gson gson = new Gson();

    public WebSocketHandler(@NotNull UserService userService, @NotNull RemotePointService remotePointService) {
        this.userService = userService;
        this.remotePointService = remotePointService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        final String user = (String) webSocketSession.getAttributes().get("user");
        if ((user == null) || (userService.getUser(user) == null)) {
            LOGGER.warn("User requested websocket is not registred or not logged in. "
                    + "Openning websocket session is denied.");
            closeSessionSilently(webSocketSession, ACCESS_DENIED);
            return;
        }
        remotePointService.registerUser(user, webSocketSession);
        try {
            String[] params = webSocketSession.getUri().toString().split("/");
            List<ChatMessage> messages = userService.getMessagesByEmails(user, params[2]);
            webSocketSession.sendMessage(new TextMessage(gson.toJson(messages)));
        } catch (IOException ignored) {}

    }

    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
        if (!webSocketSession.isOpen()) {
            return;
        }
        final String user = (String) webSocketSession.getAttributes().get("user");
        if (user == null || userService.getUser(user) == null) {
            closeSessionSilently(webSocketSession, ACCESS_DENIED);
            return;
        }
        handleMessage(user, message);
    }

    private void handleMessage(String user, TextMessage text) {
        final ChatMessage message;
        message = gson.fromJson(text.getPayload(), ChatMessage.class);
        message.setSender(user);
        try {
            remotePointService.sendMessageToUser(message.getRecipient(), message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {
        LOGGER.warn("Websocket transport problem", throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        final String user = (String) webSocketSession.getAttributes().get("user");
        //TODO:  Переподключение
        if (user == null) {
            LOGGER.warn("User disconnected but his session was not found (closeStatus=" + closeStatus + ')');
            return;
        }
        remotePointService.removeUser(user);
    }

    private void closeSessionSilently(@NotNull WebSocketSession session, @Nullable CloseStatus closeStatus) {
        final CloseStatus status = closeStatus == null ? SERVER_ERROR : closeStatus;
        //noinspection OverlyBroadCatchBlock
        try {
            session.close(status);
        } catch (Exception ignore) {
        }

    }
}
