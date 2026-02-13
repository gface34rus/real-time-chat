package com.example.chat.controller;

import com.example.chat.model.ChatMessage;
import com.example.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/**
 * Контроллер чата.
 * Обрабатывает входящие сообщения WebSocket и REST запросы для истории.
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Обрабатывает отправку сообщения.
     * Сохраняет сообщение в БД и рассылает его подписчикам комнаты.
     * 
     * @param chatMessage объект сообщения
     * @return отправленное сообщение
     */
    @MessageMapping("/chat.sendMessage")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(new Date());


        // Установка комнаты по умолчанию, если не указана
        if (chatMessage.getRoomId() == null || chatMessage.getRoomId().isEmpty()) {
            chatMessage.setRoomId("public");
        }

        // Сохраняем только сообщения типа CHAT
        if (chatMessage.getType() == com.example.chat.model.MessageType.CHAT) {
            chatMessageRepository.save(chatMessage);
        }

        // Отправка сообщения в топик конкретной комнаты
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);

        return chatMessage;
    }

    /**
     * Обрабатывает добавление (вход) пользователя.
     * Сохраняет имя пользователя и ID комнаты в атрибуты сессии WebSocket.
     * 
     * @param chatMessage    сообщение о входе
     * @param headerAccessor доступ к заголовкам и атрибутам сессии
     * @return сообщение о входе
     */
    @MessageMapping("/chat.addUser")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {

        var sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put("username", chatMessage.getSender());

            // Установка комнаты по умолчанию
            if (chatMessage.getRoomId() == null || chatMessage.getRoomId().isEmpty()) {
                chatMessage.setRoomId("public");
            }
            sessionAttributes.put("room_id", chatMessage.getRoomId());
        }

        // Уведомляем участников комнаты
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);

        return chatMessage;
    }

    /**
     * REST эндпоинт для получения истории сообщений.
     * 
     * @param roomId идентификатор комнаты (по умолчанию "public")
     * @return список сообщений для указанной комнаты
     */
    @org.springframework.web.bind.annotation.GetMapping("/api/messages")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<ChatMessage> getHistory(@RequestParam(defaultValue = "public") String roomId) {
        return chatMessageRepository.findByRoomId(roomId);
    }

}
