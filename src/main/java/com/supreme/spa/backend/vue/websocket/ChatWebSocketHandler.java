package com.supreme.spa.backend.vue.websocket;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.supreme.spa.backend.vue.models.ChatMessage;
import com.supreme.spa.backend.vue.models.User;
import com.supreme.spa.backend.vue.services.MediaService;
import com.supreme.spa.backend.vue.services.UserService;
import com.supreme.spa.backend.vue.websocket.event.GetMessages;
import com.supreme.spa.backend.vue.websocket.event.GetMessagesRouteUpdate;
import com.supreme.spa.backend.vue.websocket.event.Message;
import com.supreme.spa.backend.vue.websocket.exception.SupremeWebSocketException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import static org.springframework.web.socket.CloseStatus.SERVER_ERROR;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private static final CloseStatus ACCESS_DENIED = new CloseStatus(4500, "Not logged in. Access denied");
    private final UserService userService;
    private final RemotePointService remotePointService;
    private final Gson gson;
    private final MediaService mediaService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(UserService userService,
                                RemotePointService remotePointService,
                                Gson gson,
                                MediaService mediaService,
                                ObjectMapper objectMapper) {
        this.userService = userService;
        this.remotePointService = remotePointService;
        this.gson = gson;
        this.mediaService = mediaService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        LOGGER.info("afterConnectionEstablished");
        final String email = (String) webSocketSession.getAttributes().get("user");
        if ((email == null) || (userService.getUser(email) == null)) {
            LOGGER.warn("User requested websocket is not registred or not logged in. "
                    + "Openning websocket session is denied.");
            closeSessionSilently(webSocketSession, ACCESS_DENIED);
            return;
        }
        remotePointService.registerUser(email, webSocketSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
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

    @SuppressWarnings("Duplicates")
    @SneakyThrows({JsonParseException.class, JsonMappingException.class, IOException.class})
    private void handleMessage(int senderId, TextMessage text) {
        final Message message = objectMapper.readValue(text.getPayload(), Message.class);
        if (message.getClass() == ChatMessage.class) {
            ChatMessage chatMessage = (ChatMessage) message;
            User user = userService.getUserById(chatMessage.getRecipientId());
            chatMessage.setSenderId(senderId);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            chatMessage.setDate(timestamp);
            chatMessage.setRecipientImage(user.getImage());
            userService.addMessage(chatMessage);
            try {
                remotePointService.sendMessageToUser(user.getEmail(), chatMessage);
            } catch (SupremeWebSocketException e) {
                LOGGER.error(e.getMessage());
            }

        } else if (message.getClass() == GetMessages.class) {
            GetMessages getMessages = (GetMessages) message;
            List<ChatMessage> messages = userService.getMessagesByIds(
                    getMessages.getSenderId(), getMessages.getRecipientId());
            String recipientImage = mediaService.getLink(getMessages.getRecipientId());
            messages.forEach(m -> m.setRecipientImage(recipientImage));
            try {
                remotePointService.sendMessagesToUser(getMessages.getSenderId(), messages, getMessages.getClass());
            } catch (SupremeWebSocketException e) {
                LOGGER.error(e.getMessage());
            }
        } else if (message.getClass() == GetMessagesRouteUpdate.class) {
            GetMessagesRouteUpdate getMessages = (GetMessagesRouteUpdate) message;
            List<ChatMessage> messages = userService.getMessagesByIds(
                    getMessages.getSenderId(), getMessages.getRecipientId());
            String recipientImage = mediaService.getLink(getMessages.getRecipientId());
            messages.forEach(m -> m.setRecipientImage(recipientImage));
            try {
                remotePointService.sendMessagesToUser(getMessages.getSenderId(), messages, getMessages.getClass());
            } catch (SupremeWebSocketException e) {
                LOGGER.error(e.getMessage());
            }
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
        try {
            session.close(status);
        } catch (Exception ignore) {
        }

    }
}
