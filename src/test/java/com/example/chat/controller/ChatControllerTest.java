package com.example.chat.controller;

import com.example.chat.model.ChatMessage;
import com.example.chat.model.MessageType;
import com.example.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatMessageRepository chatMessageRepository;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    public void givenMessages_whenGetHistory_thenReturnJsonArray() throws Exception {
        ChatMessage msg1 = ChatMessage.builder()
                .sender("User1")
                .content("Hello")
                .roomId("public")
                .type(MessageType.CHAT)
                .build();

        List<ChatMessage> allMessages = Arrays.asList(msg1);

        given(chatMessageRepository.findByRoomId("public")).willReturn(allMessages);

        mockMvc.perform(get("/api/messages?roomId=public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].content", org.hamcrest.Matchers.is("Hello")));
    }
}
