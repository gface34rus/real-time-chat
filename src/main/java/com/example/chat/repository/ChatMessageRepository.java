package com.example.chat.repository;

import com.example.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью ChatMessage.
 * Предоставляет методы для доступа к базе данных.
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Находит список сообщений для указанной комнаты.
     * 
     * @param roomId идентификатор комнаты
     * @return список сообщений
     */

    List<ChatMessage> findByRoomId(String roomId);
}
