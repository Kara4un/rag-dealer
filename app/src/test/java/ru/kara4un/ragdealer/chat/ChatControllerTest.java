package ru.kara4un.ragdealer.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;

@MicronautTest
class ChatControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    GigaChatService chatService;

    @MockBean(GigaChatService.class)
    GigaChatService mockService() {
        GigaChatService mock = mock(GigaChatService.class);
        when(mock.generateReply(anyList(), anyString())).thenReturn(Mono.just("hi"));
        return mock;
    }

    @Test
    void chatReturnsAssistantReply() {
        ChatRequest req = new ChatRequest("hello");
        ChatResponse resp = client.toBlocking().retrieve(HttpRequest.POST("/api/chat", req), ChatResponse.class);
        assertEquals("hi", resp.reply());
        assertEquals(2, resp.history().size());
    }
}
