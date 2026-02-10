package com.example.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket.
 * Настраивает брокер сообщений и эндпоинты STOMP.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Регистрирует эндпоинты, к которым будут подключаться клиенты.
     * В данном случае регистрируется эндпоинт "/ws" с поддержкой SockJS.
     * 
     * @param registry реестр эндпоинтов STOMP
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    /**
     * Настраивает брокер сообщений.
     * Определяет префиксы для маршрутизации сообщений от клиента к серверу и
     * обратно.
     * 
     * @param registry реестр брокера сообщений
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Префикс для сообщений, которые обрабатываются методами с аннотацией
        // @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        // Включает простой in-memory брокер для топиков, начинающихся с "/topic"
        registry.enableSimpleBroker("/topic");
    }
}
