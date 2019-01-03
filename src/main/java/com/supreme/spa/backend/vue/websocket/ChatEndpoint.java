package com.supreme.spa.backend.vue.websocket;

import com.supreme.spa.backend.vue.models.ChatMessage;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/chat/{id}/{to}", decoders = MessageDecoder.class, encoders = MessageEncoder.class)
public class ChatEndpoint {
    private Session session;
    private static final Set<ChatEndpoint> chatEndpoints = new CopyOnWriteArraySet<>();
    private static HashMap<Integer, Session> users = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "id") Integer id) throws IOException, EncodeException {
        this.session = session;
        chatEndpoints.add(this);
        users.put(id, session);
//        ChatMessage message = new ChatMessage();
//        message.setFrom(id);
//        message.setContent("Connected!");
//        broadcast(message);
    }

    @OnMessage
    public void onMessage(@PathParam("id") Integer id,
                          @PathParam("to") Integer to,
                          ChatMessage message) throws IOException, EncodeException {
        message.setFrom(id);
        message.setTo(to);
        send(message);
    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
        chatEndpoints.remove(this);
        ChatMessage message = new ChatMessage();
//        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        broadcast(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }

    private static void broadcast(ChatMessage message) throws IOException, EncodeException {
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote()
                            .sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void send(ChatMessage message) throws IOException, EncodeException {
        users.get(message.getTo()).getBasicRemote().sendObject(message);
    }
}
