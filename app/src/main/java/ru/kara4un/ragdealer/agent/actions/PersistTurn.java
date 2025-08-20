package ru.kara4un.ragdealer.agent.actions;

import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.agent.ChatHistoryStore;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Component
public class PersistTurn {
    private final ChatHistoryStore store;

    public PersistTurn(ChatHistoryStore store) {
        this.store = store;
    }

    public void apply(String conversationId, String userMessage, String assistantMessage) {
        store.append(conversationId, new ChatMessage("user", userMessage, Instant.now()));
        store.append(conversationId, new ChatMessage("assistant", assistantMessage, Instant.now()));
    }
}
