package com.supreme.spa.backend.vue.websocket;

import com.google.gson.Gson;
import com.supreme.spa.backend.vue.models.ChatMessage;
import com.supreme.spa.backend.vue.models.User;
import com.supreme.spa.backend.vue.services.MediaService;
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
import java.util.stream.Collectors;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final CloseStatus ACCESS_DENIED = new CloseStatus(4500, "Not logged in. Access denied");
    private final UserService userService;
    private final RemotePointService remotePointService;
    private final Gson gson;
    private final MediaService mediaService;

    public WebSocketHandler(UserService userService,
                            RemotePointService remotePointService,
                            Gson gson,
                            MediaService mediaService) {
        this.userService = userService;
        this.remotePointService = remotePointService;
        this.gson = gson;
        this.mediaService = mediaService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        LOGGER.info("afterConnectionEstablished");
        final String email = (String) webSocketSession.getAttributes().get("user");
        User user = userService.getUser(email);
        if ((email == null) || (userService.getUser(email) == null)) {
            LOGGER.warn("User requested websocket is not registred or not logged in. "
                    + "Openning websocket session is denied.");
            closeSessionSilently(webSocketSession, ACCESS_DENIED);
            return;
        }
        remotePointService.registerUser(email, webSocketSession);
        try {
            String[] params = webSocketSession.getUri().toString().split("/");
            List<ChatMessage> messages = userService.getMessagesByEmails(user.getId(), Integer.parseInt(params[2]));
            String recipientImage = mediaService.getLink(Integer.parseInt(params[2]));
            messages.forEach(m -> m.setRecipientImage(recipientImage));
            webSocketSession.sendMessage(new TextMessage(gson.toJson(messages)));
        } catch (IOException ignored) {
        }

    }

    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
        LOGGER.info("handleTextMessage");
        if (!webSocketSession.isOpen()) {
            return;
        }
        final String email = (String) webSocketSession.getAttributes().get("user");
        User sender = userService.getUser(email);
        if (email == null || userService.getUser(email) == null) {
            closeSessionSilently(webSocketSession, ACCESS_DENIED);
            return;
        }
        handleMessage(sender.getId(), message);
    }

    private void handleMessage(int senderId, TextMessage text) {
        LOGGER.info("handleMessage");
        final ChatMessage message;
        message = gson.fromJson(text.getPayload(), ChatMessage.class);
        message.setSenderId(senderId);
        try {
            remotePointService.sendMessageToUser(message.getRecipientId(), message);
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
        LOGGER.info("afterConnectionClosed");
        final String user = (String) webSocketSession.getAttributes().get("user");
        //TODO:  Переподключение
        if (user == null) {
            LOGGER.warn("User disconnected but his session was not found (closeStatus=" + closeStatus + ')');
            return;
        }
        remotePointService.removeUser(user);
    }

    private void closeSessionSilently(@NotNull WebSocketSession session, @Nullable CloseStatus closeStatus) {
        LOGGER.info("closeSessionSilently");
        final CloseStatus status = closeStatus == null ? SERVER_ERROR : closeStatus;
        //noinspection OverlyBroadCatchBlock
        try {
            session.close(status);
        } catch (Exception ignore) {
        }

    }
}
