package ru.kara4un.ragdealer.chat;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import java.util.List;

@Client("${gigachat.api.base-url}")
public interface GigaChatClient {

    @Post("/chat/completions")
    GigaChatResponse completions(@Body GigaChatRequest request);

    record GigaChatRequest(List<Message> messages, String model) {}

    record Message(String role, String content) {}

    record GigaChatResponse(List<Choice> choices) {
        public record Choice(Message message) {}
    }
}
