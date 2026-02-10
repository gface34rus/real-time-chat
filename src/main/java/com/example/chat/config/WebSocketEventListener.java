package com.example.chat.config;

import com.example.chat.model.ChatMessage;
import com.example.chat.model.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Слушатель событий WebSocket.
 * Обрабатывает события подключения и отключения пользователей.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Обрабатывает событие отключения веб-сокета.
     * Извлекает имя пользователя из сессии и рассылает сообщение о выходе всем
     * участникам.
     * 
     * @param event событие отключения сессии
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            log.info("Пользователь отключился: {}", username);
            var chatMessage = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(username)
                    .build();

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
