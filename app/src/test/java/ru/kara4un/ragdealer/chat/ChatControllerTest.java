package ru.kara4un.ragdealer.chat;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kara4un.ragdealer.core.chat.InMemoryChatStore;

@WebMvcTest(ChatController.class)
@Import(InMemoryChatStore.class)
class ChatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ru.kara4un.ragdealer.agent.goals.HandleChatTurn handleChatTurn;

    @Test
    void chatReturnsAssistantReply() throws Exception {
        java.util.List<ru.kara4un.ragdealer.core.chat.ChatMessage> history =
                java.util.List.of(
                        new ru.kara4un.ragdealer.core.chat.ChatMessage("user", "hello", java.time.Instant.now()),
                        new ru.kara4un.ragdealer.core.chat.ChatMessage("assistant", "hi", java.time.Instant.now()));
        when(handleChatTurn.execute(anyString(), anyString()))
                .thenReturn(new ChatResponse("hi", history));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("hi"))
                .andExpect(jsonPath("$.history", hasSize(2)));
    }
}
