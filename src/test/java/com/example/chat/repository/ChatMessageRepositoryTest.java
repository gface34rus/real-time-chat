package com.example.chat.repository;

import com.example.chat.model.ChatMessage;
import com.example.chat.model.MessageType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    public void whenSaveMessage_thenFindIt() {
        // given
        ChatMessage message = ChatMessage.builder()
                .sender("John")
                .content("Hello")
                .type(MessageType.CHAT)
                .roomId("public")
                .timestamp(new Date())
                .build();

        // when
        chatMessageRepository.save(message);

        // then
        List<ChatMessage> found = chatMessageRepository.findAll();
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getContent()).isEqualTo("Hello");
    }

    @Test
    public void whenFindByRoomId_thenReturnOnlyRoomMessages() {
        // given
        ChatMessage msg1 = ChatMessage.builder()
                .sender("User1")
                .content("Msg1")
                .roomId("public")
                .type(MessageType.CHAT)
                .build();

        ChatMessage msg2 = ChatMessage.builder()
                .sender("User2")
                .content("Msg2")
                .roomId("work")
                .type(MessageType.CHAT)
                .build();

        chatMessageRepository.save(msg1);
        chatMessageRepository.save(msg2);

        // when
        List<ChatMessage> publicMessages = chatMessageRepository.findByRoomId("public");
        List<ChatMessage> workMessages = chatMessageRepository.findByRoomId("work");

        // then
        assertThat(publicMessages).hasSize(1);
        assertThat(publicMessages.get(0).getContent()).isEqualTo("Msg1");

        assertThat(workMessages).hasSize(1);
        assertThat(workMessages.get(0).getContent()).isEqualTo("Msg2");
    }
}
