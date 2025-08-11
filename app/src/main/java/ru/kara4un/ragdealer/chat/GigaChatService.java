package ru.kara4un.ragdealer.chat;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Singleton
public class GigaChatService {

    private final GigaChatClient client;
    private final String modelId;

    public GigaChatService(GigaChatClient client, @Value("${gigachat.api.model-id}") String modelId) {
        this.client = client;
        this.modelId = modelId;
    }

    public String generateReply(List<ChatMessage> history, String userMessage) {
        List<GigaChatClient.Message> messages = new ArrayList<>();
        for (ChatMessage msg : history) {
            messages.add(new GigaChatClient.Message(msg.role(), msg.content()));
        }
        messages.add(new GigaChatClient.Message("user", userMessage));
        GigaChatClient.GigaChatRequest request = new GigaChatClient.GigaChatRequest(messages, modelId);
        GigaChatClient.GigaChatResponse response = client.completions(request);
        return response.choices().get(0).message().content();
    }
}
