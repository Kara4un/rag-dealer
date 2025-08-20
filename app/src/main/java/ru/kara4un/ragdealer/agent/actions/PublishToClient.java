package ru.kara4un.ragdealer.agent.actions;

import java.util.List;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.chat.ChatResponse;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Component
public class PublishToClient {
    public ChatResponse apply(String reply, List<ChatMessage> history) {
        return new ChatResponse(reply, history);
    }
}
