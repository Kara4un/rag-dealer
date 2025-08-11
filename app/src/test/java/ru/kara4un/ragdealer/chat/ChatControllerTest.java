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

@MicronautTest
class ChatControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @MockBean(GigaChatClient.class)
    GigaChatClient gigaChatClient() {
        return request -> new GigaChatClient.GigaChatResponse(
                List.of(new GigaChatClient.GigaChatResponse.Choice(
                        new GigaChatClient.Message("assistant", "hi"))));
    }

    @Test
    void chatReturnsAssistantReply() {
        ChatRequest req = new ChatRequest("hello");
        ChatResponse resp = client.toBlocking().retrieve(HttpRequest.POST("/api/chat", req), ChatResponse.class);
        assertEquals("hi", resp.reply());
        assertEquals(2, resp.history().size());
    }
}
