package com.supreme.spa.backend.vue.config;

import com.supreme.spa.backend.vue.websocket.ChatWebSocketHandler;
import com.supreme.spa.backend.vue.websocket.CommonWebSocketHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
//import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.concurrent.Executors;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @NotNull
    private final ChatWebSocketHandler chatWebSocketHandler;

    private final CommonWebSocketHandler commonWebSocketHandler;

    public WebSocketConfig(@NotNull ChatWebSocketHandler chatWebSocketHandler,
                           CommonWebSocketHandler commonWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.commonWebSocketHandler = commonWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(chatWebSocketHandler, "/chat/{email}")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");

        webSocketHandlerRegistry.addHandler(commonWebSocketHandler, "/*")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");

    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());
    }
}
