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

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(new Date());

        // Default room if missing
        if (chatMessage.getRoomId() == null || chatMessage.getRoomId().isEmpty()) {
            chatMessage.setRoomId("public");
        }

        if (chatMessage.getType() == com.example.chat.model.MessageType.CHAT) {
            chatMessageRepository.save(chatMessage);
        }

        // Broadcast to specific room topic
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);

        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        var sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put("username", chatMessage.getSender());

            // Default room
            if (chatMessage.getRoomId() == null || chatMessage.getRoomId().isEmpty()) {
                chatMessage.setRoomId("public");
            }
            sessionAttributes.put("room_id", chatMessage.getRoomId());
        }

        // Notify room
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);

        return chatMessage;
    }

    @org.springframework.web.bind.annotation.GetMapping("/api/messages")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<ChatMessage> getHistory(@RequestParam(defaultValue = "public") String roomId) {
        return chatMessageRepository.findByRoomId(roomId);
    }

}
