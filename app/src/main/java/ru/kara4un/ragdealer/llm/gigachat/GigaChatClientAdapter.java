package ru.kara4un.ragdealer.llm.gigachat;

import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.kara4un.ragdealer.chat.GigaChatService;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Component
public class GigaChatClientAdapter {

    private final GigaChatService service;

    public GigaChatClientAdapter(GigaChatService service) {
        this.service = service;
    }

    public Mono<String> chat(List<ChatMessage> history, String userMessage) {
        return service.generateReply(history, userMessage);
    }
}
