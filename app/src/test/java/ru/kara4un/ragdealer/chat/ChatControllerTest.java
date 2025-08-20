package ru.kara4un.ragdealer.chat;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyList;
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
import reactor.core.publisher.Mono;
import ru.kara4un.ragdealer.core.chat.InMemoryChatStore;

@WebMvcTest(ChatController.class)
@Import(InMemoryChatStore.class)
class ChatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    GigaChatService chatService;

    @Test
    void chatReturnsAssistantReply() throws Exception {
        when(chatService.generateReply(anyList(), anyString())).thenReturn(Mono.just("hi"));

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("hi"))
                .andExpect(jsonPath("$.history", hasSize(2)));
    }
}
